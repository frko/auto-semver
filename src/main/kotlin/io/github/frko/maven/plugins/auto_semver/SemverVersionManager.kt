package io.github.frko.maven.plugins.auto_semver

import com.vdurmont.semver4j.Semver
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.BuildPluginManager
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.VersionRangeRequest
import org.twdata.maven.mojoexecutor.MojoExecutor.*

private val MAJOR_MAINLINE_VERSION_SCHEME = "(\\d+)\\.x\\.x".toRegex()

abstract class SemverVersionManager: AbstractMojo() {

    @Component
    internal lateinit var system: RepositorySystem

    @Component
    internal lateinit var plugins: BuildPluginManager

    @Parameter( defaultValue = "\${project}", readonly = true )
    internal lateinit var project: MavenProject

    @Parameter( defaultValue = "\${session}", readonly = true )
    internal lateinit var session: MavenSession

    @Parameter(defaultValue = "\${project.remoteProjectRepositories}", readonly = true)
    internal lateinit var repositories: List<RemoteRepository>

    @Parameter(defaultValue = "\${repositorySystemSession}", readonly = true)
    internal lateinit var repositorySystemSession: RepositorySystemSession

    @Parameter(property = "auto-semver.major-mainlines-only", defaultValue = "false", required = false, readonly = true)
    internal var allowOnlyMajorMainlineVersions: Boolean = false

    internal fun latestStableSemverForMajor(): Semver {
        log.info("auto-semver.major-mainlines-only = '${allowOnlyMajorMainlineVersions}'.")

        val semver = semverMajorVersion()
        log.info("managing semver major version '${semver.major}'.")

        val artifact = DefaultArtifact("${project.groupId}:${project.artifactId}:${project.packaging}:[${semver.major},${semver.nextMajor().major}]")
        val request = VersionRangeRequest(artifact, repositories, "")

        log.info("fetching versions of '${artifact.groupId}:${artifact.artifactId}:${artifact.extension}' within version range '${artifact.version}'.")
        val result = system.resolveVersionRange(repositorySystemSession, request)
        log.info("versions in reactor & repositories ['${result.versions.joinToString("', '")}'].")

        val versions = result.versions.mapNotNull { version ->
            runCatching { Semver(version.toString()) }
                .onFailure { log.warn("ignoring non semver compatible version '${version}'.") }
                .getOrNull()
        }.filter {
            val suffixes = it.getSuffixTokens().orEmpty()

            if (suffixes.isNotEmpty()) {
                log.warn("ignoring version with suffix '${it}'.")
            }

            suffixes.isEmpty()
        }.sortedWith { v1, v2 ->
            when {
                v1.isGreaterThan(v2) -> 1
                v1.isLowerThan(v2) -> -1
                else -> 0
            }
        }

        val proposal = if (versions.isNotEmpty()) {
            log.info("semver versions found ['${versions.joinToString("', '")}'].")
            val last = versions.last()
            log.info("last semver version for major '${semver.major}' = '${last}'.")
            last
        } else {
            log.info("no existing semver versions found.")
            val nothing = Semver("${semver.major}.0.0")
            log.info("semver major version for new artifact = '${semver.major}'.")
            nothing
        }

        return proposal
    }

    private fun semverMajorVersion(): Semver {
        return majorMainlineVersion()
            ?: if (allowOnlyMajorMainlineVersions) {
                throw MojoExecutionException(
                    "project.version '${project.version}' is not in a major mainline (\$major.x.x) versioning scheme."
                )
            } else {
                semverProjectVersion()
                    ?: throw MojoExecutionException(
                        "project.version '${project.version}' is not in a major mainline (\$major.x.x) or semver compatible versioning scheme."
                    )
            }
    }

    private fun majorMainlineVersion(): Semver? {
        val match = MAJOR_MAINLINE_VERSION_SCHEME.matchEntire(project.version)
            ?: return null

        val major= match.groupValues.last()

        return Semver("${major.toInt()}.0.0")
    }

    private fun semverProjectVersion(): Semver? {
        return runCatching { Semver(project.version) }
            .getOrNull()
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

    fun execute(block: () -> Unit) {
        if (project.hasParent()) {
            return log.info("skipping module '${project.name}'")
        }
        block()
    }
}
