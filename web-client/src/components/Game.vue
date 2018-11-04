<template>
    <div id="content">
        <GameCard :game="detailed" />
        <BotUploadCard v-if="loggedIn" v-on:create-bot="createBot($event)" />
        <CardPanel name="Bots" :allData="detailed.bots">
            <template slot="card" slot-scope="slotProps">
                <BotCard :bot="slotProps.elem" />
            </template>
        </CardPanel>
    </div>
</template>

<script>
    import GameCard from "./GameCard";
    import BotCard from "./BotCard";
    import CardPanel from "./CardPanel";
    import BotUploadCard from "./BotUploadCard";
    import {globalState} from "../main";

    export default {
        name: "Game",
        components: {BotUploadCard, CardPanel, BotCard, GameCard},
        data() {
            return {
                detailed: {}
            };
        },
        computed: {
            loggedIn: function () {
                return globalState.loggedInUser !== '';
            }
        },
        mounted() {
            this.load(this.$route.params.game);
        },
        watch: {
            '$route.params.game': function (newVal) {
                this.load(newVal);
            }
        },
        methods: {
            load: function (gameName) {
                this.$axios.get("games/" + gameName)
                    .then(result => this.detailed = result.data);
            },
            createBot: function (event) {
                this.$axios.post("users/" + globalState.loggedInUser + "/bots", {
                    name: event.name,
                    game: this.detailed.name,
                    jarString: event.jar
                }).then(function(){
                    alert('SUCCESS!!');
                }).catch(function(){
                    alert('FAILURE!!');
                });

            }
        }
    }
</script>

<style scoped>
    #content {
        margin: auto;
        width: 500px;
    }
</style>