<template>
    <div>
        <div class="side-panel">
            <GamePanel :user="user" />
            <GameUploadCard v-on:create-game="createGame($event)" />
        </div>
        <div class="side-panel">
            <BotPanel :user="user" />
        </div>
    </div>
</template>

<script>
    import BotPanel from "./BotPanel";
    import GamePanel from "./GamePanel";
    import {globalState} from "../main";
    import GameUploadCard from "./GameUploadCard";

    export default {
        name: "Profile",
        components: {GameUploadCard, GamePanel, BotPanel},
        data() {
            return {
                user: ''
            };
        },
        beforeMount() {
            this.setUser();
        },
        watch: {
            '$route.params.user': function () {
                this.setUser();
            }
        },
        methods: {
            setUser: function () {
                if (this.$route.params.user) {
                    this.user = this.$route.params.user;
                } else {
                    this.user = globalState.loggedInUser;
                }
            },
            createGame: function (event) {
                this.$axios.post("users/" + this.user + "/games", {
                    name: event.name,
                    isMatch: event.isMatch,
                    apiJarString: event.apiJar,
                    engineJarString: event.engineJar
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
    .side-panel {
        display: inline-block;
        vertical-align: top;
    }
</style>