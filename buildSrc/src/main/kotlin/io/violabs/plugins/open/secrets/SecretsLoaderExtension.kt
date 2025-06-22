package io.violabs.plugins.open.secrets

open class SecretsLoaderExtension {
    var secretFile: String? = null
    private var systemProperties: Map<Ext.Key, Ext.SysPropName> = mutableMapOf()

    fun systemProperties(block: SystemPropertiesMapBuilder.() -> Unit) {
        this.systemProperties = SystemPropertiesMapBuilder().apply(block).properties()
    }

    fun systemProperties(): Map<Ext.Key, Ext.SysPropName> {
        return systemProperties
    }

    class SystemPropertiesMapBuilder {
        private val systemProperties: MutableMap<Ext.Key, Ext.SysPropName> = mutableMapOf()

        fun addProperty(key: String, value: String) {
            systemProperties.put(Ext.Key(key), Ext.SysPropName(value))
        }

        fun properties(): Map<Ext.Key, Ext.SysPropName> {
            return systemProperties
        }
    }
}