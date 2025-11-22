@import hexlet.code.dto.UrlsPage
@import hexlet.code.util.NamedRoutes
@param UrlsPage page

@template.layout.page(
page = page,
content = @`
    <div class="container mt-5">
        <h1>Сайты</h1>

        <table class="table table-striped">
            <thead>
            <tr>
                <th>ID</th>
                <th>Имя</th>
                <th>Дата добавления</th>
            </tr>
            </thead>
            <tbody>
            @for(var url : page.getUrls())
                <tr>
                    <td>${url.getId()}</td>
                    <td><a href="${NamedRoutes.urlPath(url.getId())}">${url.getName()}</a></td>
                    <td>${url.getFormattedCreatedAt()}</td>
                </tr>
            @endfor
            </tbody>
        </table>
    </div>
`
)