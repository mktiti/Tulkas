<template>
    <div id="content">
        <GameCard :game="detailed" />
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

    export default {
        name: "Game",
        components: {CardPanel, BotCard, GameCard},
        data() {
            return {
                detailed: {}
            };
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