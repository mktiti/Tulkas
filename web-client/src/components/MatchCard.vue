<template>
    <div id="card">
        <div class="bot-holder">
            <BotCard :bot="match.botA" :class="botClass('A')" />
        </div>
        <div id="vs-label">
            vs
        </div>
        <div class="bot-holder">
            <BotCard :bot="match.botB" :class="botClass('B')" />
        </div>
    </div>
</template>

<script>
    import BotCard from "./BotCard";

    export default {
        name: "MatchCard",
        components: {BotCard},
        props: [
            'match'
        ],
        methods: {
            botClass(name) {
                if (this.match.result === 'BOT_' + name + '_WIN') {
                    return 'win';
                } else if (this.match.result === 'BOT_' + name + '_ERROR') {
                    return 'error';
                } else if (this.match.result === 'DRAW') {
                    return 'draw';
                } else {
                    return '';
                }
            }
        }
    }
</script>

<style scoped>
    #card {
        margin: 20px;
        text-align: left;
    }

    .bot-holder {
        display: inline-block;
        width: 45%;
    }

    .bot-holder>#card {
        margin-left: 0;
        margin-right: 0;
    }

    #card a {
        text-decoration: none;
    }

    #vs-label {
        width: 10%;
        display: inline-block;
        font-size: x-large;
        text-align: center;
        vertical-align: super;
    }

    .bot-holder>.win {
        background: green;
    }
    .bot-holder>.error {
        background: darkred;
    }
    .bot-holder>.draw {
        background: yellow;
    }
</style>