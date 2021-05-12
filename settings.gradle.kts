rootProject.name = "ceylon-gradle-plugin"

include("example")
include("example:sample")
include("example:sample")
findProject(":example:sample")?.name = "sample"
