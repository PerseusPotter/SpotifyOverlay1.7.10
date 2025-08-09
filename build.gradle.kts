plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

version = "1.1.0"