<template>
    <div id="card">
        <span id="name-label">Upload new game</span>
        <div id="details">
            <label for="name-in">Name</label>
            <input id="name-in" v-model="name"/>
            <br />
            <input id="match-in" type="checkbox" name="match" value="match" v-model="isMatch">Two player match
            <br />
            <label for="api-in">Api jar data</label>
            <input id="api-in" type="file" ref="apiIn" accept=".jar" @change="loadApi"/>
            <br />
            <label for="engine-in">Engine jar data</label>
            <input id="engine-in" type="file" ref="engineIn" accept=".jar" @change="loadEngine"/>
            <br />
            <button v-if="name && apiJar && engineJar" @click="createGame">Create</button>
        </div>
    </div>
</template>

<script>
    export default {
        name: "GameUploadCard",
        data() {
            return {
                name: '',
                isMatch: false,
                apiJar: '',
                engineJar: ''
            };
        },
        methods: {
            loadApi() {
                let reader  = new FileReader();

                let _this = this;
                reader.addEventListener("load", function () {
                    let res = reader.result;
                    _this.apiJar = res.substr(res.indexOf(',') + 1);
                }, false);

                let apiFile = this.$refs.apiIn.files[0];
                if (apiFile) {
                    this.apiJar = '';
                    reader.readAsDataURL(apiFile);
                }

            },
            loadEngine() {
                let reader  = new FileReader();

                let _this = this;
                reader.addEventListener("load", function () {
                    let res = reader.result;
                    _this.engineJar = res.substr(res.indexOf(',') + 1);
                }, false);

                let engineFile = this.$refs.engineIn.files[0];
                if (engineFile) {
                    this.engineJar = '';
                    reader.readAsDataURL(engineFile);
                }

            },
            createGame: function () {
                this.$emit('create-game', {
                    name: this.name,
                    isMatch: this.isMatch,
                    apiJar: this.apiJar,
                    engineJar: this.engineJar
                });
            }
        }
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