<template>
    <div>
        <h1>Games</h1>
        <input id="game-search" v-model="search" @keyup.enter="queryGames" />
        <button @click="queryGames">Search</button>

        <div id="games">
            <GameCard v-for="game in games" :key="game.name" v-bind:game="game" />
        </div>
    </div>
</template>

<script>
    import GameCard from "./GameCard";

    export default {
        name: "Games",
        components: {GameCard},
        data() {
            return {
                search: '',
                games: []
            }
        },
        mounted: function () {
            this.queryGames(this.search);
        },
        methods: {
            queryGames: function () {
                this.$axios.get("games?s=" + this.search)
                    .then(games => this.games = games.data);
            }
        }
    }
</script>

<style scoped>
    #games {
        margin: auto;
        width: 400px;
    }
</style>