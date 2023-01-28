package io.telereso.kmp.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * ThrowableSerializer is a custom serializer for the Throwable class
 * which is used to serialize/deserialize Throwable objects to/from json strings.
 * This class implements the KSerializer interface provided by the kotlinx.serialization library.
 *
 * @property descriptor is the serial descriptor for the Throwable class.
 *
 * @function serialize is used to serialize a Throwable object into a json string.
 * It takes an encoder object and a Throwable object as input and returns a json string representation of the object
 *
 * @function deserialize is used to deserialize a json string into a Throwable object.
 * It takes a decoder object as input and returns a Throwable object
 */
object ThrowableSerializer : KSerializer<Throwable> {
    override val descriptor = PrimitiveSerialDescriptor("Throwable", PrimitiveKind.STRING)

    /**
     * Serialize a Throwable object into a json string.
     * @param encoder the encoder object used to serialize the Throwable object
     * @param value the Throwable object to be serialized
     */
    override fun serialize(encoder: Encoder, value: Throwable) = encoder.encodeString(value.toString())

    /**
     * Deserialize a json string into a Throwable object.
     * @param decoder the decoder object used to deserialize the json string
     */
    override fun deserialize(decoder: Decoder): Throwable = Throwable(decoder.decodeString())
}