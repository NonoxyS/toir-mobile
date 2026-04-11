package ru.mirea.toir.feature.auth.impl.data.mappers

import ru.mirea.toir.common.mappers.Mapper
import ru.mirea.toir.core.auth.domain.models.DomainAuthUser
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteUser

internal interface AuthUserMapper : Mapper<RemoteUser, DomainAuthUser>

internal class AuthUserMapperImpl : AuthUserMapper {
    override fun map(item: RemoteUser): DomainAuthUser = DomainAuthUser(
        id = item.id.orEmpty(),
        displayName = item.displayName.orEmpty(),
        role = item.role.orEmpty(),
    )
}
