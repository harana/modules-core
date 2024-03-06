import sbt._
import com.harana.sbt.common._

val modulesCore = haranaProject("modules-core").in(file("."))
	.settings(
    libraryDependencies ++=
      Library.jackson.value ++
      Library.logging.value ++
      Library.micrometer.value ++
      Library.okhttp.value ++
      Library.testing.value ++
      Library.vertx.value ++
      Library.zio2.value :+
      Library.commonsIo.value :+
      Library.ficus.value :+
      Library.json.value :+
      Library.mongodbScala.value :+
      Library.scaffeine.value :+
      Library.sourcecode.value :+
      Library.ulid.value :+
      "com.harana" %%% "sdk" % "1.0.0"
    )