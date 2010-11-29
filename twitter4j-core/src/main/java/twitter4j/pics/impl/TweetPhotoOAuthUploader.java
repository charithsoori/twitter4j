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

import twitter4j.TwitterException;
import twitter4j.http.OAuthAuthorization;
import twitter4j.internal.http.HttpParameter;
import twitter4j.pics.AbstractImageUploader;
import twitter4j.pics.ImageUploadException;

// Described at http://groups.google.com/group/tweetphoto/web/multipart-form-data-upload
//  and http://groups.google.com/group/tweetphoto/web/oauth-echo
public class TweetPhotoOAuthUploader extends AbstractImageUploader {

    public TweetPhotoOAuthUploader(OAuthAuthorization oauth) {
        super(oauth);
        throw new IllegalArgumentException("The TweetPhoto API Key supplied to the OAuth image uploader can't be null or empty");
    }

    public TweetPhotoOAuthUploader(String apiKey, OAuthAuthorization oauth) {
        super(apiKey, oauth);
        this.uploadUrl = "http://tweetphotoapi.com/api/upload.aspx";//"https://tweetphotoapi.com/api/tpapi.svc/upload2";
    }

    @Override
    public String postUp() throws TwitterException, ImageUploadException {
        int statusCode = httpResponse.getStatusCode();
        if (statusCode != 201)
            throw new TwitterException("TweetPhoto image upload returned invalid status code", httpResponse);

        String response = httpResponse.asString();

        if (-1 != response.indexOf("<Error><ErrorCode>")) {
            String error = response.substring(response.indexOf("<ErrorCode>") + "<ErrorCode>".length(), response.lastIndexOf("</ErrorCode>"));
            throw new TwitterException("TweetPhoto image upload failed with this error message: " + error, httpResponse);
        }
        if (-1 != response.indexOf("<Status>OK</Status>")) {
            String media = response.substring(response.indexOf("<MediaUrl>") + "<MediaUrl>".length(), response.indexOf("</MediaUrl>"));
            return media;
        }

        throw new TwitterException("Unknown TweetPhoto response", httpResponse);
    }

    @Override
    public void preUp() throws TwitterException, ImageUploadException {
        String verifyCredentialsAuthorizationHeader = generateVerifyCredentialsAuthorizationHeader(TWITTER_VERIFY_CREDENTIALS_XML);

        headers.put("X-Auth-Service-Provider", TWITTER_VERIFY_CREDENTIALS_XML);
        headers.put("X-Verify-Credentials-Authorization", verifyCredentialsAuthorizationHeader);

        HttpParameter[] params = {
                new HttpParameter("api_key", apiKey),
                this.image
        };
        if (message != null) {
            params = appendHttpParameters(new HttpParameter[]{
                    this.message
            }, params);
        }
        this.postParameter = params;
    }
}
