package me.exzork.gcauth.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.database.DatabaseHelper;
import emu.grasscutter.game.Account;
import emu.grasscutter.utils.Utils;
import me.exzork.gcauth.GCAuth;
import me.exzork.gcauth.json.AuthResponseJson;
import me.exzork.gcauth.json.RegisterAccount;
import me.exzork.gcauth.utils.Authentication;

import java.io.IOException;

public class RegisterHandler extends AbstractHandler{
    @Override
    public void handle(HttpExchange t) throws IOException {
        AuthResponseJson authResponse = new AuthResponseJson();

        if (GCAuth.getConfig().Enable) {
            try {
                String requestBody = Utils.toString(t.getRequestBody());
                if (requestBody.isEmpty()) {
                    authResponse.success = false;
                    authResponse.message = "EMPTY_BODY"; // ENG = "No data was sent with the request"
                    authResponse.jwt = "";
                } else {
                    RegisterAccount registerAccount = new Gson().fromJson(requestBody, RegisterAccount.class);
                    if (registerAccount.password.equals(registerAccount.password_confirmation)) {
                        String password = Authentication.generateHash(registerAccount.password);
                        Account account = DatabaseHelper.createAccountWithPassword(registerAccount.username, password);
                        if (account == null) {
                            authResponse.success = false;
                            authResponse.message = "USERNAME_TAKEN"; // ENG = "Username has already been taken by another user."
                            authResponse.jwt = "";
                        } else {
                            authResponse.success = true;
                            authResponse.message = "";
                            authResponse.jwt = "";
                        }
                    } else {
                        authResponse.success = false;
                        authResponse.message = "PASSWORD_MISMATCH"; // ENG = "Passwords do not match."
                        authResponse.jwt = "";
                    }
                }
            } catch (Exception e) {
                authResponse.success = false;
                authResponse.message = "UNKNOWN"; // ENG = "An unknown error has occurred..."
                authResponse.jwt = "";
                Grasscutter.getLogger().error("[Dispatch] An error occurred while creating an account.");
                e.printStackTrace();
            }
        } else {
            authResponse.success = false;
            authResponse.message = "AUTH_DISABLED"; // ENG = "Authentication is not required for this server..."
            authResponse.jwt = "";
        }

        responseJSON(t, authResponse);
    }
}
