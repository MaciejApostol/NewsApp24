import {createAsyncThunk, createSlice} from "@reduxjs/toolkit";
import {DESC, PENDING, REJECTED, FULFILLED} from "../constants/constants";
import {newsAPI} from "../utils/newsAPI";

export const initialState = {
    response: {
        news: {},
        status: "idle",
        error: null,
    },
    sort: {
        name: "Latest",
        category: "datePublished",
        order: DESC
    },
    page: 1,
    articlesOnPage: 10
};

export const fetchNews = createAsyncThunk(
    'news/fetchNews',
    async (city) => {

        const response = await newsAPI.fetchNews(city);
        let newVar = await response.json();
        console.log(newVar);
        return newVar;
    },
);


const scrollTop = () => {
    window.scrollTo(0, 0);
};

const newsSlice = createSlice({
        name: "news",
        initialState,
        reducers: {
            setSort: (state, action) => {
                state.page = 1;
                state.sort = action.payload;
            },
            setArticlesOnPage: (state, action) => {
                state.articlesOnPage = action.payload;
            },
            setPage: (state, action) => {
                state.page = action.payload;
                scrollTop();
            }
        },
        extraReducers: (builder) => {
            builder
                .addCase(fetchNews.pending, (state) => {
                    state.response.status = PENDING;
                })
                .addCase(fetchNews.fulfilled, (state, action) => {
                    state.response.status = FULFILLED;
                    state.response.news = action.payload;
                    state.page = 1;
                    state.sort = initialState.sort;
                    scrollTop();
                })
                .addCase(fetchNews.rejected, (state) => {
                    state.response.status = REJECTED;
                    state.response.error = "Unknown Error";
                    console.warn("rejected");
                });
        },
        selectors: {
            selectNews: state => state.response.news,
            selectArticles: state => state.response.news.articles,
            selectStatus: state => state.response.status,
            selectError: state => state.response.error,
            selectSort: state => state.sort,
            selectPage: state => state.page,
            selectArticlesOnPage: state => state.articlesOnPage,
        },
    }
);

export const {
    selectNews, selectArticles,
    selectStatus, selectSort,
    selectPage, selectArticlesOnPage
} = newsSlice.selectors;
export const {
    setSort, setArticlesOnPage,
    setPage
} = newsSlice.actions;
export default newsSlice.reducer;