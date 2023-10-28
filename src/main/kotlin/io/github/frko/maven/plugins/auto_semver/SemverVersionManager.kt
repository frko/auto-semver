package io.github.frko.maven.plugins.auto_semver

import com.vdurmont.semver4j.Semver
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.BuildPluginManager
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.VersionRangeRequest
import org.twdata.maven.mojoexecutor.MojoExecutor.*

abstract class SemverVersionManager: AbstractMojo() {

    @Component
    internal lateinit var system: RepositorySystem

    @Component
    internal lateinit var project: MavenProject

    @Component
    internal lateinit var session: MavenSession

    @Component
    internal lateinit var plugins: BuildPluginManager

    @Parameter(defaultValue = "\${project.remoteProjectRepositories}", readonly = true)
    internal lateinit var repositories: List<RemoteRepository>

    @Parameter(defaultValue = "\${repositorySystemSession}", readonly = true)
    internal lateinit var repositorySystemSession: RepositorySystemSession

    internal fun latestStableSemverForMajor(): Semver {
        val semver = Semver(project.version)
        log.info("managing semver major version '${semver.major}'")

        val artifact = DefaultArtifact("com.bol.api1st.rage:rage-parent:pom:[${semver.major}.0.0,${semver.nextMajor().major}.0.0]")
        val request = VersionRangeRequest(artifact, repositories, "")

        log.info("fetching versions of '${artifact.groupId}:${artifact.artifactId}:${artifact.extension}' within version range '${artifact.version}'")
        val result = system.resolveVersionRange(repositorySystemSession, request)

        println(result)

        val versions = result.versions.mapNotNull { version ->
            runCatching { Semver(version.toString()) }.onFailure {
                log.warn("ignoring non semver compatible version '${version}'")
            }.getOrNull()
        }.filter {
            val suffixes = it.getSuffixTokens().orEmpty()

            if (suffixes.isNotEmpty()) {
                log.warn("ignoring version with suffix '${it}'")
            }

            suffixes.isEmpty()
        }.sortedWith { v1, v2 ->
            when {
                v1.isGreaterThan(v2) -> 1
                v1.isLowerThan(v2) -> -1
                else -> 0
            }
        }

        log.info("semver versions found ['${versions.joinToString("', '")}']")

        val latest = versions.lastOrNull() ?: Semver("${semver.major}.0.0")
        log.info("last semver version for major '${semver.major}' = '${latest}'")

        return latest
    }

    internal fun updateSemverVersion(version: Semver) {
        executeMojo(plugin(groupId("org.codehaus.mojo"), artifactId("versions-maven-plugin"), version("2.16.1")),
            goal("set"),
            configuration(
                element(name("generateBackupPoms"), "false"),
                element(name("newVersion"), version.toString()),
            ),
            executionEnvironment(project, session, plugins));
    }
}
