<template>
    <div id="login-form">
        <p class="alert-message" v-if="invalidLogin">Invalid username or password</p>
        <label for="username-input">Username</label>
        <input id="username-input" v-model="username" />
        <label for="password-input">Password</label>
        <input id="password-input" v-model="password" type="password" />
        <button @click="login">Login</button>
    </div>
</template>

<script>
    export default {
        name: "Login",
        data() {
            return {
                invalidLogin: false,
                username: '',
                password: ''
            }
        },
        methods: {
            login: function () {
                let success = this.password === 'password';
                this.invalidLogin = !success;
                if (success) {
                    this.$emit('login-success', this.username)
                }
            }
        }
    }
</script>

<style scoped>
    .alert-message {
        color: red;
        margin-bottom: 15px;
        font-size: large;
    }

    #login-form {
        margin: auto;
        width: 300px;
        text-align: left;
        padding-top: 50px;
        padding-bottom: 50px;
    }

    input {
        font-size: x-large;
        margin-bottom: 20px;
        padding: 5px 20px 5px 10px;
        border: 2px solid darkgray;
        border-radius: 2px;
    }

    input:focus {
        border-color: #4d6bec;
    }

    label {
        display: block;
        margin-bottom: 2px;
    }

    button {
        display: block;
        font-size: x-large;
        padding: 5px 20px 5px 20px;
        border: 2px solid darkgray;
        border-radius: 2px;
        margin-top: 15px;
    }

    button:hover {
        border-color: #4d6bec;
    }
</style>