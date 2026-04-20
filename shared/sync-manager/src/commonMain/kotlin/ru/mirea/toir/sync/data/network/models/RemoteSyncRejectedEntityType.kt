package ru.mirea.toir.sync.data.network.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.mirea.toir.core.network.deserializer.enumSerializerWithFallback

@Serializable(with = RemoteSyncRejectedEntityTypeSerializer::class)
internal enum class RemoteSyncRejectedEntityType {
    INSPECTION,
    INSPECTION_EQUIPMENT_RESULT,
    CHECKLIST_ITEM_RESULT,
    UNKNOWN,
}

internal object RemoteSyncRejectedEntityTypeSerializer : KSerializer<RemoteSyncRejectedEntityType> {
    private val delegate = enumSerializerWithFallback(
        fallbackEnum = RemoteSyncRejectedEntityType.UNKNOWN,
        serialNameMapping = mapOf(
            RemoteSyncRejectedEntityType.INSPECTION to "inspection",
            RemoteSyncRejectedEntityType.INSPECTION_EQUIPMENT_RESULT to "inspectionEquipmentResult",
            RemoteSyncRejectedEntityType.CHECKLIST_ITEM_RESULT to "checklistItemResult",
        )
    )
    override val descriptor = delegate.descriptor
    override fun serialize(encoder: Encoder, value: RemoteSyncRejectedEntityType) = delegate.serialize(encoder, value)
    override fun deserialize(decoder: Decoder): RemoteSyncRejectedEntityType = delegate.deserialize(decoder)
}
