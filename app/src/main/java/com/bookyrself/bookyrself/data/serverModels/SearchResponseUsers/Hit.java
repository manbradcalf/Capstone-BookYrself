package com.bookyrself.bookyrself.data.serverModels.SearchResponseUsers;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Hit {

    @SerializedName("_id")
    private String m_id;
    @SerializedName("_index")
    private String m_index;
    @SerializedName("_score")
    private Double m_score;
    @SerializedName("_source")
    private com.bookyrself.bookyrself.data.serverModels.SearchResponseUsers._source m_source;
    @SerializedName("_type")
    private String m_type;

    public String get_id() {
        return m_id;
    }

    public void set_id(String _id) {
        m_id = _id;
    }

    public String get_index() {
        return m_index;
    }

    public void set_index(String _index) {
        m_index = _index;
    }

    public Double get_score() {
        return m_score;
    }

    public void set_score(Double _score) {
        m_score = _score;
    }

    public com.bookyrself.bookyrself.data.serverModels.SearchResponseUsers._source get_source() {
        return m_source;
    }

    public void set_source(com.bookyrself.bookyrself.data.serverModels.SearchResponseUsers._source _source) {
        m_source = _source;
    }

    public String get_type() {
        return m_type;
    }

    public void set_type(String _type) {
        m_type = _type;
    }

}
