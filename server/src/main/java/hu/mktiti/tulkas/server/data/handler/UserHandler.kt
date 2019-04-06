package hu.mktiti.tulkas.server.data.handler

import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.dto.*
import hu.mktiti.tulkas.server.data.repo.BotRepo
import hu.mktiti.tulkas.server.data.repo.GameLogRepo
import hu.mktiti.tulkas.server.data.repo.GameRepo
import hu.mktiti.tulkas.server.data.repo.UserRepo
import hu.mktiti.tulkas.server.data.security.LoginRequired
import hu.mktiti.tulkas.server.data.service.GameManager
import javax.inject.Singleton
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/users")
@Singleton
class UserHandler(
        private val userRepo: UserRepo = inject(),
        private val gameRepo: GameRepo = inject(),
        private val botRepo: BotRepo = inject(),
        private val gameLogRepo: GameLogRepo = inject(),
        private val gameManager: GameManager = inject()
) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun listAll(@QueryParam("s") search: String?): List<SimpleUserDto> {
        val list = if (search == null) userRepo.listAll() else userRepo.searchNameContaining(search)
        return list.map { SimpleUserDto(it.name) }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    fun createUser(
            userData: UserUploadData
    ): Response = pathCreated("/users/${userData.name}") {
        userRepo.createUser(userData.name, userData.password)
    }

    @Path("{username}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getUser(
            @PathParam("username") username: String
    ): Response = entity {
        val user = userRepo.findByName(username) ?: return@entity null
        DetailedUserDto(
                name = user.name,
                games = gameRepo.gamesOf(user.id).toSimpleDtos(username),
                bots = botRepo.botsOf(username).toSimpleDtos(username)
        )
    }

    @Path("{username}/bots")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getBots(
            @PathParam("username") username: String
    ): List<SimpleBotDto> = botRepo.botsOf(username).toSimpleDtos(username)


    @Path("{username}/bots")
    @POST
    @LoginRequired(usernameParam = "username")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun uploadBot(
            @PathParam("username") username: String,
            botData: BotUploadData
    ): Response {
        val user = userRepo.findByName(username) ?: return badRequest("User not found")
        val game = gameRepo.findByName(botData.game) ?: return badRequest("Game not found")

        return pathCreated("users/$username/bots/${botData.name}") {
            botRepo.createBot(
                    ownerId = user.id,
                    gameId  = game.id,
                    name    = botData.name,
                    jar     = fromBase64(botData.jarString)
            )?.apply {
                gameManager.onNewBot(this)
            }
        }
    }

    @Path("{username}/bots/{botName}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getBots(
            @PathParam("username") username: String,
            @PathParam("botName") botName: String
    ): Response {
        val bot = botRepo.botByUserAndName(username, botName) ?: return notFound()
        val game = gameRepo.find(bot.gameId) ?: return notFound()

        val simpleBot = bot.toSimpleDto(username, game.name)

        val played = if (game.isMatch) {
            gameLogRepo.matchesOfBot(bot.id).map { fromMatch(simpleBot, it) }
        } else {
            gameLogRepo.challengesOfBot(bot.id).map { fromChallenge(simpleBot, it) }
        }

        return entity {
            DetailedBotData(
                    name = bot.name,
                    ownerUsername = username,
                    game = game.name,
                    played = played,
                    rank = bot.rank
            )
        }
    }

    @Path("{username}/games")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getGames(
            @PathParam("username") username: String
    ): Response = entity {
        val user = userRepo.findByName(username) ?: return@entity null
        gameRepo.gamesOf(user.id).toSimpleDtos(username)
    }

    @Path("{username}/games")
    @POST
    @LoginRequired(usernameParam = "username")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun uploadBot(
            @PathParam("username") username: String,
            uploadData: GameUploadData
    ): Response {
        val user = userRepo.findByName(username) ?: return badRequest("User not found")

        return pathCreated("users/$username/games/${uploadData.name}") {
            gameRepo.createGame(
                    ownerId = user.id,
                    name = uploadData.name,
                    isMatch = uploadData.isMatch,
                    apiJar = fromBase64(uploadData.apiJarString),
                    engineJar = fromBase64(uploadData.engineJarString)
            )
        }
    }

}