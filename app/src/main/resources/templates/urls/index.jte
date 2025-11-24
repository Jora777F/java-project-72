@import hexlet.code.dto.UrlsPage
@import hexlet.code.util.NamedRoutes
@param UrlsPage page

@template.layout.page(
page = page,
content = @`
    <div class="container mt-5">
        <h1>Сайты</h1>

        <table class="table table-bordered table-hover">
            <thead>
            <tr>
                <th>ID</th>
                <th>Имя</th>
                <th>Последняя проверка</th>
                <th>Код ответа</th>
            </tr>
            </thead>
            <tbody>
            @if(page.getUrls().isEmpty())
                <tr>
                    <td colspan="4" class="text-center">Пока нет добавленных страниц</td>
                </tr>
            @else
                @for(var url : page.getUrls())
                    <tr>
                        <td>${url.getId()}</td>
                        <td><a href="${NamedRoutes.urlPath(url.getId())}">${url.getName()}</a></td>
                        <td>
                            @if(page.getLastChecks().containsKey(url.getId()))
                                ${page.getLastChecks().get(url.getId()).getFormattedCreatedAt()}
                            @endif
                        </td>
                        <td>
                            @if(page.getLastChecks().containsKey(url.getId()))
                                ${page.getLastChecks().get(url.getId()).getStatusCode()}
                            @endif
                        </td>
                    </tr>
                @endfor
            @endif
            </tbody>
        </table>
    </div>
`
)