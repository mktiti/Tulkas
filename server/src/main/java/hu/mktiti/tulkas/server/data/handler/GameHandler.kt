package hu.mktiti.tulkas.server.data.handler

import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.dto.DetailedGameDto
import hu.mktiti.tulkas.server.data.dto.SimpleBotDto
import hu.mktiti.tulkas.server.data.dto.SimpleGameDto
import hu.mktiti.tulkas.server.data.dto.toSimpleDtos
import hu.mktiti.tulkas.server.data.repo.BotRepo
import hu.mktiti.tulkas.server.data.repo.GameRepo
import hu.mktiti.tulkas.server.data.repo.UserRepo
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/games")
class GameHandler(
        private val userRepo: UserRepo = inject(),
        private val gameRepo: GameRepo = inject(),
        private val botRepo: BotRepo   = inject()
) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun allGames(@QueryParam("s") search: String?): List<SimpleGameDto> {
        val list = if (search == null || search.isBlank()) gameRepo.listWithOwnerName() else gameRepo.searchWithOwnerName(search)
        return list.toSimpleDtos()
    }

    @Path("{gameName}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getGame(
            @PathParam("gameName") gameName: String
    ): Response {
        val game  = gameRepo.findByName(gameName) ?: return notFound()
        val owner = userRepo.find(game.ownerId) ?: return notFound()
        val bots  = botRepo.botsByGame(game.id)

        return entity {
            DetailedGameDto(
                    name = game.name,
                    isMatch = game.isMatch,
                    owner = owner.name,
                    bots = bots.map {
                        SimpleBotDto(
                                name = it.first.name,
                                game = game.name,
                                ownerUsername = it.second
                        )
                    }
            )
        }
    }

}