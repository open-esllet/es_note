# NOTE

The EsNote app, part of the ESBook ecosystem, is primarily designed for note-taking.

# GRADLE Configuration

If you are not in China, please modify the following configuration:
## settings.gradle.kts
Remove `maven.aliyun.com` from the repositories
## gradle-wrapper.properties
`distributionUrl=https\://mirrors.cloud.tencent.com/gradle/gradle-x.y.z-all.zip`<br>
modify to<br>
`distributionUrl=https\://services.gradle.org/distributions/gradle-x.y.z-bin.zip`