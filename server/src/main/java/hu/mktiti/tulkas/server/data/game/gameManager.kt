package hu.mktiti.tulkas.server.data.game

sealed class Game(
       val apiJar: ByteArray,
       val engineJar: ByteArray,
       val botA: ByteArray
)