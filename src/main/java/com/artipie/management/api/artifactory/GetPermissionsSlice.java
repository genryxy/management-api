/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 artipie.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.management.api.artifactory;

import com.amihaiemil.eoyaml.YamlMapping;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.rs.common.RsJson;
import com.artipie.management.RepoPermissions;
import java.nio.ByteBuffer;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import org.reactivestreams.Publisher;

/**
 * Artifactory `GET /api/security/permissions` endpoint, returns
 * permissions ( = repositories) list.
 * @since 0.1
 */
public final class GetPermissionsSlice implements Slice {

    /**
     * Repository permissions.
     */
    private final RepoPermissions permissions;

    /**
     * Artipie meta config.
     */
    private final YamlMapping meta;

    /**
     * Ctor.
     * @param permissions Repository permissions
     * @param meta Artipie meta config
     */
    public GetPermissionsSlice(final RepoPermissions permissions, final YamlMapping meta) {
        this.permissions = permissions;
        this.meta = meta;
    }

    @Override
    public Response response(final String line, final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body) {
        final String base = this.meta.string("base_url").replaceAll("/$", "");
        return new AsyncResponse(
            this.permissions.repositories().<Response>thenApply(
                list -> {
                    final JsonArrayBuilder json = Json.createArrayBuilder();
                    list.forEach(
                        perm -> json.add(GetPermissionsSlice.permJson(base, perm))
                    );
                    return new RsJson(json);
                }
            )
        );
    }

    /**
     * Returns json for repo permission.
     * @param base Base url
     * @param name Repo permission name
     * @return User json object
     */
    private static JsonObject permJson(final String base, final String name) {
        return Json.createObjectBuilder()
            .add("name", name)
            .add("uri", String.format("%s/api/security/permissions/%s", base, name))
            .build();
    }
}
