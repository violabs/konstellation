package io.violabs.konstellation.metaDsl

import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible

/**
 * Ensures the value returned by the given property reference is not `null`.
 *
 * @param accessor the property reference being validated
 * @return the non-null value of [accessor]
 */
fun <T> vRequireNotNull(accessor: KProperty<T?>): T {
    accessor.isAccessible = true
    return requireNotNull(accessor.getter.call()) { "${accessor.name} is required" }
}

//region COLLECTION
/**
 * Common implementation for [vRequireCollectionNotEmpty] variants.
 */
private fun <T, C : Collection<T>> requireCollectionNotEmptyInternal(value: C?, name: String): C {
    val returnedValue = if (value?.isEmpty() != false) null else value
    return requireNotNull(returnedValue) { "$name is required and cannot be empty" }
}

/**
 * Validates that the supplied list is not null or empty.
 */
fun <T, C : Collection<T>> vRequireCollectionNotEmpty(value: C?, name: String): C =
    requireCollectionNotEmptyInternal(value, name)

/**
 * Takes in a classes property accessor and validates that is not null or blank.
 * It used the accessor name as the default within the exception message.
 * You may find problems using with nested class functions.
 */
fun <T, C : Collection<T>> vRequireCollectionNotEmpty(accessor: KProperty<C?>): C {
    accessor.isAccessible = true
    return requireCollectionNotEmptyInternal(accessor.call(), accessor.name)
}

/**
 * Takes in a classes function accessor and validates that is not null or blank.
 * It used the accessor name as the default within the exception message.
 * You may find problems using with nested class functions.
 */
fun <T, C : Collection<T>> vRequireCollectionNotEmpty(accessor: KFunction<C?>): C {
    accessor.isAccessible = true
    return requireCollectionNotEmptyInternal(accessor.call(), accessor.name)
}
//endregion COLLECTION

//region MAP
/**
 * Common implementation for [vRequireMapNotEmpty] variants.
 */
private fun <K, V, M : Map<K, V>> requireMapNotEmptyInternal(map: M?, name: String): M {
    val returnedValue = if (map?.isEmpty() != false) null else map
    return requireNotNull(returnedValue) { "$name is required and cannot be empty" }
}

/**
 * Validates that the supplied list is not null or empty.
 */
fun <K, V, M : Map<K, V>> vRequireMapNotEmpty(map: M?, name: String): M =
    requireMapNotEmptyInternal(map, name)

/**
 * Takes in a classes property accessor and validates that is not null or blank.
 * It used the accessor name as the default within the exception message.
 * You may find problems using with nested class functions.
 */
fun <K, V, M : Map<K, V>> vRequireMapNotEmpty(accessor: KProperty<M?>): M {
    accessor.isAccessible = true
    return requireMapNotEmptyInternal(accessor.call(), accessor.name)
}

/**
 * Takes in a classes function accessor and validates that is not null or blank.
 * It used the accessor name as the default within the exception message.
 * You may find problems using with nested class functions.
 */
fun <K, V, M : Map<K, V>> vRequireMapNotEmpty(accessor: KFunction<M?>): M {
    accessor.isAccessible = true
    return requireMapNotEmptyInternal(accessor.call(), accessor.name)
}
//endregion MAP