<template>
    <div id="card">
        <span id="name-label">Upload new bot</span>
        <div id="details">
            <label for="name-in">Name</label>
            <input id="name-in" v-model="name"/>
            <br />
            <label for="jar-in">Jar data</label>
            <input id="jar-in" type="file" ref="jarIn" accept=".jar" @change="loadJar"/>
            <br />
            <button v-if="name && jar" @click="createBot">Create</button>
        </div>
    </div>
</template>

<script>
    export default {
        name: "BotUploadCard",
        data() {
            return {
                name: '',
                jar: ''
            };
        },
        methods: {
            loadJar() {
                let reader  = new FileReader();

                let _this = this;
                reader.addEventListener("load", function () {
                    let res = reader.result;
                    _this.jar = res.substr(res.indexOf(',') + 1);
                }, false);

                let jarFile = this.$refs.jarIn.files[0];
                if (jarFile) {
                    this.jar = '';
                    reader.readAsDataURL(jarFile);
                }

            },
            createBot: function () {
                this.$emit('create-bot', {
                    name: this.name,
                    jar: this.jar
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