<template>
    <div>
        <h1>{{ name }}</h1>
        <input id="search" v-model="filter" @keyup.enter="filterData" />
        <button @click="filterData">Search</button>

        <div id="data" v-if="filteredData.length > 0">
            <div v-for="elem in filteredData" v-bind:key="elem.name">
                <slot name="card" v-bind:elem="elem" />
            </div>
        </div>
        <div v-else>
            <p>No matches found</p>
        </div>
    </div>
</template>

<script>
    export default {
        name: "CardPanel",
        props: [
            'name',
            'allData'
        ],
        data() {
            return {
                filter: '',
                filteredData: []
            };
        },
        watch: {
            allData: function () {
                this.filterData();
            }
        },
        mounted: function () {
            this.filterData();
        },
        methods: {
            filterData: function () {
                if (this.filter === '') {
                    this.filteredData = this.allData;
                } else {
                    this.filteredData = this.allData.filter(elem => elem.name.toLowerCase().includes(this.filter.toLowerCase()));
                }
            }
        }
    }
</script>

<style scoped>
    #data {
        margin: auto;
        width: 500px;
    }
</style>