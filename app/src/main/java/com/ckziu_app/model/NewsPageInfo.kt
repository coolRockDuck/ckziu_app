package com.ckziu_app.model

/** Wrapper class for holding list of news and some extra info about page
 * @param listOfNewsResponses list of news on page
 * @param maxPageIndex number of pages of news
 * */
data class NewsPageInfo(
    val listOfNewsResponses: List<News>,
    val maxPageIndex: Int?
) {
    companion object {

        /**Transforms `Result<NewsPageInfo>` into ` Result<List<News>>`
         *  and returns the same subtype of [Result].
         *
         *  If [newsPageInfoResult] is type [Success] then
         *  extracts [list of news][NewsPageInfo.listOfNewsResponses] from [NewsPageInfo]
         *  and passes it to the constructor of a 'Success<List<News>>()'
         *
         *       Success<NewsPageInfo> -> Success<List<News>>
         *       Failure<NewsPageInfo> -> Failure<List<News>>
         *
         *  */
        fun fromNewsPageInfoResultToNewsResult(newsPageInfoResult: Result<NewsPageInfo>): Result<List<News>> {
            return when (newsPageInfoResult) {
                is Success<NewsPageInfo> -> {
                    Success(newsPageInfoResult.resultValue.listOfNewsResponses)
                }
                is InProgress<NewsPageInfo> -> InProgress()
                is Failure<NewsPageInfo> -> Failure(
                    newsPageInfoResult.errorMsg,
                    newsPageInfoResult.error
                )
            }
        }
    }
}