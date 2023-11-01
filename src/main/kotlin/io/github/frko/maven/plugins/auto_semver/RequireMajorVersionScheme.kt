package io.github.frko.maven.plugins.auto_semver

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo

@Mojo(name = "require-major-mainline-version-scheme", defaultPhase = LifecyclePhase.NONE, threadSafe = true)
class RequireMajorVersionScheme: SemverVersionManager() {

    override fun execute() {
        majorMainlineVersion() ?: throw MojoExecutionException(
            "project.version '${project.version}' is not in a major mainline (\$major.x.x) versioning scheme."
        )

        if (project.parent != null) {
            if (project.parent.version != project.version) throw MojoExecutionException(
                "project.version '${project.version}' is not equal to project.parent.version '${project.parent.version}'."
            )
        }
    }
}
