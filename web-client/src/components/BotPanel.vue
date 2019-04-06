<template>
    <CardPanel name="Bots" :allData="data">
        <template slot="card" slot-scope="slotProps">
            <BotCard :bot="slotProps.elem" />
        </template>
    </CardPanel>
</template>

<script>
    import BotCard from "./BotCard";
    import CardPanel from "./CardPanel";
    import {globalState} from "../main";

    export default {
        name: "BotPanel",
        components: {CardPanel, BotCard},
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
                let path = 'users/' + user + "/bots";
                this.$axios.get(path).then(resp => this.data = resp.data);
            }
        }
    }
</script>

<style scoped>
</style>