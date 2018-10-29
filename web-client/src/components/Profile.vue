<template>
    <div>
        <GamePanel :user="user" />
        <BotPanel  :user="user" />
    </div>
</template>

<script>
    import BotPanel from "./BotPanel";
    import GamePanel from "./GamePanel";
    import {globalState} from "../main";

    export default {
        name: "Profile",
        components: {GamePanel, BotPanel},
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
            }
        }
    }
</script>

<style scoped>

</style>