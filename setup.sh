#!/usr/bin/env bash
set -euo pipefail

# ─────────────────────────────────────────────
#  KMMTemplate setup script
#  Usage: ./setup.sh --package com.example.myapp --app-name MyApp
# ─────────────────────────────────────────────

usage() {
    echo "Usage: $0 --package <package> --app-name <AppName>"
    echo ""
    echo "  --package    Full package name (e.g. com.example.myapp)"
    echo "  --app-name   App name in PascalCase (e.g. MyApp)"
    echo ""
    echo "Example:"
    echo "  $0 --package com.example.myapp --app-name MyApp"
    exit 1
}

# ── Parse arguments ─────────────────────────
NEW_PACKAGE=""
NEW_APP_NAME=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --package)   NEW_PACKAGE="$2";   shift 2 ;;
        --app-name)  NEW_APP_NAME="$2";  shift 2 ;;
        *)           usage ;;
    esac
done

[[ -z "$NEW_PACKAGE" || -z "$NEW_APP_NAME" ]] && usage

# ── Validate package name ────────────────────
if ! [[ "$NEW_PACKAGE" =~ ^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*){2,}$ ]]; then
    echo "Error: package must be lowercase, at least 3 segments (e.g. com.example.myapp)"
    exit 1
fi

# ── Derive values ────────────────────────────
OLD_PACKAGE="dev.nonoxy.kmmtemplate"
OLD_PACKAGE_PATH="dev/nonoxy/kmmtemplate"
OLD_APP_NAME="KMMTemplate"
OLD_APP_NAME_PASCAL="KmmTemplate"
OLD_APP_NAME_CAMEL="kmmTemplate"

NEW_PACKAGE_PATH="${NEW_PACKAGE//.//}"
# camelCase: lowercase first letter of NEW_APP_NAME
NEW_APP_NAME_CAMEL="$(echo "${NEW_APP_NAME:0:1}" | tr '[:upper:]' '[:lower:]')${NEW_APP_NAME:1}"

echo ""
echo "  App name : $OLD_APP_NAME / $OLD_APP_NAME_PASCAL / $OLD_APP_NAME_CAMEL"
echo "          → $NEW_APP_NAME / $NEW_APP_NAME / $NEW_APP_NAME_CAMEL"
echo "  Package  : $OLD_PACKAGE → $NEW_PACKAGE"
echo "  Path     : $OLD_PACKAGE_PATH → $NEW_PACKAGE_PATH"
echo ""
read -rp "Continue? [y/N] " confirm
[[ "$confirm" =~ ^[Yy]$ ]] || { echo "Aborted."; exit 0; }

# ── Portable in-place sed ────────────────────
replace_in_file() {
    local from="$1" to="$2" file="$3"
    perl -pi -e "s|\Q${from}\E|${to}|g" "$file"
}

# ── Replace content in source files ─────────
echo "Replacing file contents..."

find . \
    -type f \
    \( -name "*.kt" -o -name "*.kts" -o -name "*.xml" \
    -o -name "*.xcconfig" -o -name "*.plist" \
    -o -name "template.xml" -o -name "*.ftl" \) \
    -not -path "./.git/*" \
    -not -path "./build/*" \
    -not -path "**/build/*" \
    -not -path "./.gradle/*" \
    | while read -r file; do
        replace_in_file "$OLD_PACKAGE_PATH"      "$NEW_PACKAGE_PATH"      "$file"
        replace_in_file "$OLD_PACKAGE"          "$NEW_PACKAGE"           "$file"
        replace_in_file "$OLD_APP_NAME"         "$NEW_APP_NAME"          "$file"
        replace_in_file "$OLD_APP_NAME_PASCAL"  "$NEW_APP_NAME"          "$file"
        replace_in_file "$OLD_APP_NAME_CAMEL"   "$NEW_APP_NAME_CAMEL"    "$file"
    done

# ── Rename source directories ────────────────
echo "Renaming source directories..."

find . -type d -name "kotlin" \
    -not -path "./.git/*" \
    -not -path "*/build/*" \
    | while read -r src_root; do
        old_dir="${src_root}/${OLD_PACKAGE_PATH}"
        new_dir="${src_root}/${NEW_PACKAGE_PATH}"

        if [[ -d "$old_dir" ]]; then
            mkdir -p "$(dirname "$new_dir")"
            cp -r "$old_dir" "$new_dir"
            rm -rf "${src_root}/dev"
        fi
    done

echo ""
echo "Done! Next steps:"
echo "  1. Open the project in Android Studio / IntelliJ IDEA"
echo "  2. Check iosApp/Configuration/Config.xcconfig — set your TEAM_ID"
echo "  3. Sync Gradle and build"
