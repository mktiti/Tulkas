<template>
    <div id="card">
        <div class="bot-holder">
            <BotCard :bot="match.botA" />
        </div>
        <div id="vs-label">
            vs
        </div>
        <div class="bot-holder">
            <BotCard :bot="match.botB" />
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
        ]
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
</style>