<template>
    <div>
        <table id="info-panel">
            <tr>
                <BotCard :bot="detailed" />
            </tr>
            <tr>
                <h2>Matches</h2>
            </tr>
            <tr v-for="m in detailed.played" :class="{ selected: (selectedMatch === m.id) }" id="match-log" :key="m.id" v-on:click="selectMatch(m.id)">
                <MatchCard :match="m" />
            </tr>
        </table>
        <div id="log-panel">
            <h2>Log Entries</h2>
            <pre :v-if="logEntries.size > 0" v-for="(log, index) in logEntries" :key="index"
               :class="{ fromEngine: (log.sender === 'ENGINE'), fromSelf: (log.sender === 'BOT_A' || log.sender === 'BOT_B'), fromTulkas: (log.sender === 'TULKAS') }">[{{ log.sender }}] {{ prefixEntry(log.message) }}</pre>
            <i v-if="selectedMatch !== null && logEntries.size === 0">
                Match contains no log entries
            </i>
            <i v-if="selectedMatch === null">
                Select match to load log messages
            </i>
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
            },
            prefixEntry: function(entry) {
                let tmp = entry.split('\n'), res = [];

                for (const frag of tmp) {
                    res.push(`\t${frag}`);
                }

                return res.join('\n');
            }
        }
    }
</script>

<style scoped>
    #info-panel, #log-panel {
        width: 50%;
        display: inline-block;
    }

    #match-log>#card {
        margin-top: 0;
    }

    #match-log.selected {
        background: #d8d8d8;
    }

    #log-panel {
        margin-top: 20px;
        vertical-align: top;
    }

    #log-panel>h2 {
        margin-bottom: 20px;
    }

    pre {
        font-family: monospace;
    }

    #log-panel>pre {
        text-align: left;
        line-height: 1.4;
    }

    #log-panel>pre.fromEngine {
        color: dodgerblue;
    }

    #log-panel>pre.fromTulkas {
        color: orange;
    }
</style>