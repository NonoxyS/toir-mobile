#!/usr/bin/env ruby
# Adds dev/prod build configurations + schemes to the single iosApp target.
# obj77-safe: assigns base xcconfig via explicit PBXFileReference (the gem does
# not model the anchor/relativePath base-config form, so duplicated configs would
# otherwise lose Config.xcconfig). Idempotent: removes prior dev/prod configs first.
require "xcodeproj"

proj_path = ARGV[0] or abort "usage: setup_envs.rb <path.xcodeproj>"
proj = Xcodeproj::Project.open(proj_path)
target = proj.targets.first

# --- xcconfig file references (one each, reused for project + target configs) ---
def xcconfig_ref(proj, rel)
  existing = proj.files.find { |f| f.path == rel }
  return existing if existing
  ref = proj.main_group.new_file(rel)
  ref.last_known_file_type = "text.xcconfig"
  ref
end

dev_xc = xcconfig_ref(proj, "Configuration/Config-dev.xcconfig")
prod_xc = xcconfig_ref(proj, "Configuration/Config-prod.xcconfig")

ENVS = {
  "Debug-dev" => { src: "Debug", xc: dev_xc },
  "Release-dev" => { src: "Release", xc: dev_xc },
  "Debug-prod" => { src: "Debug", xc: prod_xc },
  "Release-prod" => { src: "Release", xc: prod_xc },
}

def rebuild_configs(list, proj)
  src_cfgs = {}
  list.build_configurations.each { |c| src_cfgs[c.name] = c }
  # drop any previously generated env configs (idempotent re-run)
  list.build_configurations.delete_if { |c| ENVS.key?(c.name) }
  ENVS.each do |name, spec|
    src = src_cfgs[spec[:src]]
    nc = proj.new(Xcodeproj::Project::Object::XCBuildConfiguration)
    nc.name = name
    nc.build_settings = src.build_settings.dup
    # KGP embedAndSign + moko read $CONFIGURATION to pick debug/release; the
    # custom config names confuse them, so pin the framework build type.
    nc.build_settings["KOTLIN_FRAMEWORK_BUILD_TYPE"] = (spec[:src] == "Debug" ? "debug" : "release")
    nc.base_configuration_reference = spec[:xc]
    list.build_configurations << nc
  end
end

# project-level: just needs the config names to exist
rebuild_configs(proj.build_configuration_list, proj)
# target-level: names + base xcconfig (carries APP_ENVIRONMENT, bundle id)
rebuild_configs(target.build_configuration_list, proj)

# keep Debug/Release as the default; schemes pick the env config explicitly
proj.build_configuration_list.default_configuration_name = "Release"
target.build_configuration_list.default_configuration_name = "Release"

proj.save

# --- schemes ---
def make_scheme(proj, proj_path, target, name, debug_cfg, release_cfg)
  s = Xcodeproj::XCScheme.new
  s.add_build_target(target)
  s.set_launch_target(target)
  s.build_action.entries.each { |e| e.build_for_running = true }
  s.test_action.build_configuration = debug_cfg
  s.launch_action.build_configuration = debug_cfg
  s.profile_action.build_configuration = release_cfg
  s.analyze_action.build_configuration = debug_cfg
  s.archive_action.build_configuration = release_cfg
  s.save_as(proj_path, name, true) # shared
end

make_scheme(proj, proj_path, target, "toir-dev", "Debug-dev", "Release-dev")
make_scheme(proj, proj_path, target, "toir-prod", "Debug-prod", "Release-prod")

# remove the old auto scheme name if shared variant exists (leave user scheme alone)
out = []
out << "configs=#{Xcodeproj::Project.open(proj_path).targets.first.build_configurations.map(&:name).sort.join(',')}"
out << "schemes=#{Dir.glob(File.join(proj_path, 'xcshareddata/xcschemes/*.xcscheme')).map { |p| File.basename(p) }.sort.join(',')}"
File.write("/tmp/setup_envs_result.txt", out.join("\n"))
