<template>
    <CardPanel name="Games" :allData="data">
        <template slot="card" slot-scope="slotProps">
            <GameCard :game="slotProps.elem" />
        </template>
    </CardPanel>
</template>

<script>
    import GameCard from "./GameCard";
    import CardPanel from "./CardPanel";
    import {globalState} from "../main";

    export default {
        name: "GamePanel",
        components: {CardPanel, GameCard},
        props: [
            'user'
        ],
        data() {
            return {
                data: []
            };
        },
        watch: {
            user: function(newVal) {
                this.updateData(newVal);
            }
        },
        mounted: function () {
            this.updateData(this.user);
        },
        methods: {
            updateData: function (user) {
                if (!user) {
                    user = globalState.loggedInUser;
                }
                let path = 'users/' + user + "/games";
                this.$axios.get(path).then(resp => this.data = resp.data);
            }
        }
    }
</script>

<style scoped>
</style>