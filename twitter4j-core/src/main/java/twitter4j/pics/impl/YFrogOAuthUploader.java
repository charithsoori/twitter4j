/*
Copyright (c) 2007-2010, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package twitter4j.pics.impl;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.OAuthAuthorization;
import twitter4j.internal.http.HttpParameter;
import twitter4j.pics.AbstractImageUploader;
import twitter4j.pics.ImageUploadException;

public class YFrogOAuthUploader extends AbstractImageUploader {

    public YFrogOAuthUploader(OAuthAuthorization oauth) {
        super(oauth);
    }

    @Override
    public String postUp() throws TwitterException, ImageUploadException {
        int statusCode = httpResponse.getStatusCode();
        if (statusCode != 200) {
            throw new TwitterException("YFrog image upload returned invalid status code", httpResponse);
        }

        String response = httpResponse.asString();
        if (-1 != response.indexOf("<rsp stat=\"fail\">")) {
            String error = response.substring(response.indexOf("msg") + 5, response.lastIndexOf("\""));
            throw new TwitterException("YFrog image upload failed with this error message: " + error, httpResponse);
        }
        if (-1 != response.indexOf("<rsp stat=\"ok\">")) {
            String media = response.substring(response.indexOf("<mediaurl>") + "<mediaurl>".length(), response.indexOf("</mediaurl>"));
            return media;
        }

        throw new TwitterException("Unknown YFrog response", httpResponse);
    }

    @Override
    public void preUp() throws TwitterException, ImageUploadException {
        uploadUrl = "https://yfrog.com/api/upload";
        String signedVerifyCredentialsURL = generateVerifyCredentialsAuthorizationURL(TWITTER_VERIFY_CREDENTIALS_XML);
        Twitter tw = new TwitterFactory().getInstance(this.oauth);

        HttpParameter[] params = {
                new HttpParameter("auth", "oauth"),
                new HttpParameter("username", tw.verifyCredentials().getScreenName()),
                new HttpParameter("verify_url", signedVerifyCredentialsURL),
                this.image,
        };
        if (message != null) {
            params = appendHttpParameters(new HttpParameter[]{
                    this.message
            }, params);
        }
        this.postParameter = params;
    }
}
