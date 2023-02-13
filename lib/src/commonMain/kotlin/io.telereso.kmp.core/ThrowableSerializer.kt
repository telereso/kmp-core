/*
 * MIT License
 *
 * Copyright (c) 2023 Telereso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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