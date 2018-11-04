<template>
    <div>
        <table id="info-panel">
            <tr>
                <BotCard :bot="detailed" />
            </tr>
            <tr>
                <div id="match-label">Matches</div>
            </tr>
            <tr v-for="m in detailed.played" :class="{ selected: (selectedMatch === m.id) }" id="match-log" :key="m.id" v-on:click="selectMatch(m.id)">
                <MatchCard :match="m" />
            </tr>
        </table>
        <div id="log-panel">
            Log Entries
            <p :v-if="logEntries.size > 0" v-for="(log, index) in logEntries" :key="index">
                {{ log.sender }} -> {{ log.target }}
                {{ log.message }}
            </p>
        </div>
    </div>
</template>

<script>
    import BotCard from "./BotCard";
    import MatchCard from "./MatchCard";

    export default {
        name: "Bot",
        components: {MatchCard, BotCard},
        data() {
            return {
                detailed: {},
                logEntries: [],
                selectedMatch: null
            };
        },
        mounted() {
            this.load(this.$route.params.user, this.$route.params.bot);
        },
        watch: {
            '$route.params.bot': function (newVal) {
                this.load(this.$route.params.user, newVal);
            },
            '$route.params.user': function (newVal) {
                this.load(newVal, this.$route.params.bot);
            },
            'selectedMatch': function (newVal) {
                if (newVal == null) {
                    this.logEntries = [];
                } else {
                    this.loadEntries(newVal);
                }
            }
        },
        methods: {
            load: function (user, bot) {
                this.detailed = [];
                this.selectedMatch = null;
                this.$axios.get("users/" + user + "/bots/" + bot)
                    .then(result => this.detailed = result.data);
            },
            loadEntries: function (matchId) {
                this.$axios.get("matches/" + matchId)
                    .then(result => this.logEntries = result.data);
            },
            selectMatch: function (matchId) {
                if (this.selectedMatch === matchId) {
                    this.selectedMatch = null;
                } else {
                    this.selectedMatch = matchId;
                }
            }
        }
    }
</script>

<style scoped>
    #info-panel, #log-panel {
        width: 50%;
        display: inline-block;
    }

    #match-label {
        font-size: x-large;
    }

    #match-log>#card {
        margin-top: 0;
    }

    #match-log.selected {
        background: yellow;
    }
</style>