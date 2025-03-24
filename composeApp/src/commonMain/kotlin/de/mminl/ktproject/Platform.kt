package de.mminl.ktproject

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform