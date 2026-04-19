package ru.mirea.toir.feature.auth.impl.data.mappers

import ru.mirea.toir.common.mappers.Mapper
import ru.mirea.toir.core.auth.domain.models.DomainAuthUser
import ru.mirea.toir.core.auth.domain.models.UserRole
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteUser
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteUserRole

internal interface AuthUserMapper : Mapper<RemoteUser, DomainAuthUser>

internal class AuthUserMapperImpl : AuthUserMapper {
    override fun map(item: RemoteUser): DomainAuthUser = DomainAuthUser(
        id = item.id,
        displayName = item.displayName,
        role = item.role.toDomain(),
    )

    private fun RemoteUserRole.toDomain(): UserRole = when (this) {
        RemoteUserRole.EXECUTOR -> UserRole.EXECUTOR
        RemoteUserRole.ADMIN -> UserRole.ADMIN
        RemoteUserRole.UNKNOWN -> UserRole.UNKNOWN
    }
}
