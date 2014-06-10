/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.util.array;

import io.airlift.slice.SizeOf;

import java.util.Arrays;

import static com.facebook.presto.util.array.BigArrays.INITIAL_SEGMENTS;
import static com.facebook.presto.util.array.BigArrays.SEGMENT_SIZE;
import static com.facebook.presto.util.array.BigArrays.offset;
import static com.facebook.presto.util.array.BigArrays.segment;
import static io.airlift.slice.SizeOf.sizeOfObjectArray;

// Note: this code was forked from fastutil (http://fastutil.di.unimi.it/)
// Copyright (C) 2010-2013 Sebastiano Vigna
public final class ObjectBigArray<T>
{
    private static final long SIZE_OF_SEGMENT = sizeOfObjectArray(SEGMENT_SIZE);

    private Object[][] array;
    private int capacity;
    private int segments;

    /**
     * Creates a new big array containing one initial segment
     */
    public ObjectBigArray()
    {
        array = new Object[INITIAL_SEGMENTS][];
        allocateNewSegment();
    }

    public ObjectBigArray(Object defaultValue)
    {
        array = new Object[INITIAL_SEGMENTS][];
        allocateNewSegment(defaultValue);
    }

    /**
     * Returns the size of this big array in bytes.
     */
    public long sizeOf()
    {
        return SizeOf.sizeOf(array) + (segments * SIZE_OF_SEGMENT);
    }

    /**
     * Returns the element of this big array at specified index.
     *
     * @param index a position in this big array.
     * @return the element of this big array at the specified position.
     */
    public T get(long index)
    {
        return (T) array[segment(index)][offset(index)];
    }

    /**
     * Sets the element of this big array at specified index.
     *
     * @param index a position in this big array.
     */
    public void set(long index, T value)
    {
        array[segment(index)][offset(index)] = value;
    }

    /**
     * Ensures this big array is at least the specified length.  If the array is smaller, segments
     * are added until the array is larger then the specified length.
     */
    public void ensureCapacity(long length)
    {
        if (capacity > length) {
            return;
        }

        grow(length);
    }

    public void ensureCapacity(long length, Object defaultValue)
    {
        if (capacity > length) {
            return;
        }

        grow(length, defaultValue);
    }

    private void grow(long length)
    {
        // how many segments are required to get to the length?
        int requiredSegments = segment(length) + 1;

        // grow base array if necessary
        if (array.length < requiredSegments) {
            array = Arrays.copyOf(array, requiredSegments);
        }

        // add new segments
        while (segments < requiredSegments) {
            allocateNewSegment();
        }
    }

    private void grow(long length, Object defaultValue)
    {
        // how many segments are required to get to the length?
        int requiredSegments = segment(length) + 1;

        // grow base array if necessary
        if (array.length < requiredSegments) {
            array = Arrays.copyOf(array, requiredSegments);
        }

        // add new segments
        while (segments < requiredSegments) {
            allocateNewSegment(defaultValue);
        }
    }

    private void allocateNewSegment()
    {
        array[segments] = new Object[SEGMENT_SIZE];
        capacity += SEGMENT_SIZE;
        segments++;
    }

    private void allocateNewSegment(Object defaultValue)
    {
        Object[] newSegment = new Object[SEGMENT_SIZE];
        Arrays.fill(newSegment, defaultValue);
        array[segments] = newSegment;
        capacity += SEGMENT_SIZE;
        segments++;
    }
}
