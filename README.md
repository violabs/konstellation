# konstellation

A robust DSL generation library for generating Kotlin DSL.

> :warning: **This is a work in progress.**

## Logging
### Tiered Logging Example

The `Logger` methods now accept optional `tier` and `branch` parameters
that help format nested log output with connecting lines.

```kotlin
val logger = Logger("DSL_BUILDER").enableDebug()

logger.debug("+++ DOMAIN: MyDomain +++")
logger.debug("package: com.example", tier = 1, branch = true)
logger.debug("type: MyDomain", tier = 1, branch = true)
logger.debug("Properties", tier = 1)
logger.debug("myProperty", tier = 2, branch = true)
logger.debug("type: kotlin.String", tier = 3)
```

Should have a structure if there are nested processes:

```
konstellation DEBUG [····DSL_BUILDER] *>> +++ DOMAIN: io.violabs.konstellation.starCharts.loki.bloomBuild.BloomBuildPlannerQueue  +++
konstellation DEBUG [····DSL_BUILDER] *>>   |__ package: io.violabs.konstellation.starCharts.loki.bloomBuild
konstellation DEBUG [····DSL_BUILDER] *>>   |__ type: BloomBuildPlannerQueue
konstellation DEBUG [····DSL_BUILDER] *>>   |__ builder: BloomBuildPlannerQueueBuilder
konstellation DEBUG [····DSL_BUILDER] *>>   |__ DSL Marker added
konstellation DEBUG [····DSL_BUILDER] *>>   |__ DSL Builder Interface added
konstellation DEBUG [····DSL_BUILDER] *>>   |__ Properties added
konstellation DEBUG [····DSL_BUILDER] *>>       |__ maxQueuedTasksPerTenant
konstellation DEBUG [····DSL_BUILDER] *>>       |   |__ type: kotlin.Int
konstellation DEBUG [····DSL_BUILDER] *>>       |   |__ singleEntryTransform: null
konstellation DEBUG [····DSL_BUILDER] *>>       |__ storeTasksOnDisk
konstellation DEBUG [····DSL_BUILDER] *>>       |   |__ type: kotlin.Boolean
konstellation DEBUG [····DSL_BUILDER] *>>       |   |__ singleEntryTransform: null
konstellation DEBUG [····DSL_BUILDER] *>>       |__ tasksDiskDirectory
konstellation DEBUG [····DSL_BUILDER] *>>       |   |__ type: kotlin.String
konstellation DEBUG [····DSL_BUILDER] *>>       |   |__ singleEntryTransform: null
konstellation DEBUG [····DSL_BUILDER] *>>       |__ cleanTasksDirectory
konstellation DEBUG [····DSL_BUILDER] *>>           |__ type: kotlin.Boolean
konstellation DEBUG [····DSL_BUILDER] *>>           |__ singleEntryTransform: null
```