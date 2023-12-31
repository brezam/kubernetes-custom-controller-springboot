/*
 * Kubernetes
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: v1.21.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.zamariola.bruno.eventhubcontroller.models;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * V1alpha1EventhubSpec
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-07-12T13:58:23.910Z[Etc/UTC]")
public class V1alpha1EventhubSpec {
  /**
   * Gets or Sets authorizationClaims
   */
  @JsonAdapter(AuthorizationClaimsEnum.Adapter.class)
  public enum AuthorizationClaimsEnum {
    MANAGE("manage"),
    
    SEND("send"),
    
    LISTEN("listen");

    private String value;

    AuthorizationClaimsEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static AuthorizationClaimsEnum fromValue(String value) {
      for (AuthorizationClaimsEnum b : AuthorizationClaimsEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    public static class Adapter extends TypeAdapter<AuthorizationClaimsEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final AuthorizationClaimsEnum enumeration) throws IOException {
        jsonWriter.value(enumeration.getValue());
      }

      @Override
      public AuthorizationClaimsEnum read(final JsonReader jsonReader) throws IOException {
        String value =  jsonReader.nextString();
        return AuthorizationClaimsEnum.fromValue(value);
      }
    }
  }

  public static final String SERIALIZED_NAME_AUTHORIZATION_CLAIMS = "authorizationClaims";
  @SerializedName(SERIALIZED_NAME_AUTHORIZATION_CLAIMS)
  private List<AuthorizationClaimsEnum> authorizationClaims = new ArrayList<>();

  public static final String SERIALIZED_NAME_AUTHORIZATION_NAME = "authorizationName";
  @SerializedName(SERIALIZED_NAME_AUTHORIZATION_NAME)
  private String authorizationName;

  public static final String SERIALIZED_NAME_EVENT_HUB_INSTANCE_NAME = "eventHubInstanceName";
  @SerializedName(SERIALIZED_NAME_EVENT_HUB_INSTANCE_NAME)
  private String eventHubInstanceName;

  public static final String SERIALIZED_NAME_EVENT_HUB_RESOURCE_GROUP = "eventHubResourceGroup";
  @SerializedName(SERIALIZED_NAME_EVENT_HUB_RESOURCE_GROUP)
  private String eventHubResourceGroup;


  public V1alpha1EventhubSpec authorizationClaims(List<AuthorizationClaimsEnum> authorizationClaims) {
    
    this.authorizationClaims = authorizationClaims;
    return this;
  }

  public V1alpha1EventhubSpec addAuthorizationClaimsItem(AuthorizationClaimsEnum authorizationClaimsItem) {
    this.authorizationClaims.add(authorizationClaimsItem);
    return this;
  }

   /**
   * Get authorizationClaims
   * @return authorizationClaims
  **/
  @ApiModelProperty(required = true, value = "")

  public List<AuthorizationClaimsEnum> getAuthorizationClaims() {
    return authorizationClaims;
  }


  public void setAuthorizationClaims(List<AuthorizationClaimsEnum> authorizationClaims) {
    this.authorizationClaims = authorizationClaims;
  }


  public V1alpha1EventhubSpec authorizationName(String authorizationName) {
    
    this.authorizationName = authorizationName;
    return this;
  }

   /**
   * Get authorizationName
   * @return authorizationName
  **/
  @ApiModelProperty(required = true, value = "")

  public String getAuthorizationName() {
    return authorizationName;
  }


  public void setAuthorizationName(String authorizationName) {
    this.authorizationName = authorizationName;
  }


  public V1alpha1EventhubSpec eventHubInstanceName(String eventHubInstanceName) {
    
    this.eventHubInstanceName = eventHubInstanceName;
    return this;
  }

   /**
   * Get eventHubInstanceName
   * @return eventHubInstanceName
  **/
  @ApiModelProperty(required = true, value = "")

  public String getEventHubInstanceName() {
    return eventHubInstanceName;
  }


  public void setEventHubInstanceName(String eventHubInstanceName) {
    this.eventHubInstanceName = eventHubInstanceName;
  }


  public V1alpha1EventhubSpec eventHubResourceGroup(String eventHubResourceGroup) {
    
    this.eventHubResourceGroup = eventHubResourceGroup;
    return this;
  }

   /**
   * Get eventHubResourceGroup
   * @return eventHubResourceGroup
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public String getEventHubResourceGroup() {
    return eventHubResourceGroup;
  }


  public void setEventHubResourceGroup(String eventHubResourceGroup) {
    this.eventHubResourceGroup = eventHubResourceGroup;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1alpha1EventhubSpec v1alpha1EventhubSpec = (V1alpha1EventhubSpec) o;
    return Objects.equals(this.authorizationClaims, v1alpha1EventhubSpec.authorizationClaims) &&
        Objects.equals(this.authorizationName, v1alpha1EventhubSpec.authorizationName) &&
        Objects.equals(this.eventHubInstanceName, v1alpha1EventhubSpec.eventHubInstanceName) &&
        Objects.equals(this.eventHubResourceGroup, v1alpha1EventhubSpec.eventHubResourceGroup);
  }

  @Override
  public int hashCode() {
    return Objects.hash(authorizationClaims, authorizationName, eventHubInstanceName, eventHubResourceGroup);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1alpha1EventhubSpec {\n");
    sb.append("    authorizationClaims: ").append(toIndentedString(authorizationClaims)).append("\n");
    sb.append("    authorizationName: ").append(toIndentedString(authorizationName)).append("\n");
    sb.append("    eventHubInstanceName: ").append(toIndentedString(eventHubInstanceName)).append("\n");
    sb.append("    eventHubResourceGroup: ").append(toIndentedString(eventHubResourceGroup)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

