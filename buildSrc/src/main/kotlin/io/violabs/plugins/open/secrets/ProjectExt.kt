package io.violabs.plugins.open.secrets

import org.gradle.api.Project

/**
 * Extension functions for the [Project] class to retrieve access ID and secret key
 * from either project properties or environment variables.
 * These functions are useful for tasks that require
 * access to sensitive information like API keys or credentials.
 * @param propertyLookup The name of the property to look up in the project.
 * @param envLookup The name of the environment variable to look up if the property is not found.
 * @return The value of the property or environment variable, or null if not found.
 */
fun Project.getPropertyOrEnv(
    propertyLookup: String,
    envLookup: String? = null
): String? {
    return (findProperty(propertyLookup) as String?) ?: System.getenv(envLookup ?: propertyLookup)
}