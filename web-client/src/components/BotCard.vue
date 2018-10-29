<template>
    <div id="card">
        <span id="name-label">
            <router-link :to="'users/' + bot.ownerUsername + '/bots/' + bot.name" >{{ bot.name }}</router-link>
        </span>
        <div id="details">
            <div>
                by <router-link :to="'/users/' + bot.ownerUsername" >{{ bot.ownerUsername }}</router-link>
            </div>
            <div>
                plays <router-link :to="'/games/' + bot.game" >{{ bot.game }}</router-link>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        name: "BotCard",
        props: [
            'bot'
        ]
    }
</script>

<style scoped>
    #card {
        padding: 30px;
        border: 2px black solid;
        margin: 20px;
        text-align: left;
    }

    #details {
        display: inline-block;
        padding-left: 20px;
        vertical-align: middle;
    }

    #name-label {
        font-size: x-large;
        vertical-align: middle;
    }

    #card a {
        text-decoration: none;
    }
</style>