@import hexlet.code.dto.UrlPage
@import hexlet.code.util.NamedRoutes
@param UrlPage page

@template.layout.page(
page = page,
content = @`
    <div class="container mt-4">
        <h1 class="mb-4">Сайт: ${page.getUrl().getName()}</h1>

        <table class="table table-bordered">
            <tbody>
            <tr>
                <th>ID</th>
                <td>${page.getUrl().getId()}</td>
            </tr>
            <tr>
                <th>Имя</th>
                <td>${page.getUrl().getName()}</td>
            </tr>
            <tr>
                <th>Дата создания</th>
                <td>${page.getUrl().getFormattedCreatedAt()}</td>
            </tr>
            </tbody>
        </table>

        <h2 class="mt-5 mb-3">Проверки</h2>
        <form action="${NamedRoutes.urlChecksPath(page.getUrl().getId())}" method="post">
            <button type="submit" class="btn btn-primary mb-3">Запустить проверку</button>
        </form>

        <table class="table table-bordered table-striped">
            <thead>
            <tr>
                <th>ID</th>
                <th>Код ответа</th>
                <th>title</th>
                <th>h1</th>
                <th>description</th>
                <th>Дата проверки</th>
            </tr>
            </thead>
            <tbody>
            @if(page.getUrlChecks().isEmpty())
                <tr>
                    <td colspan="6" class="text-center">Пока нет проверок</td>
                </tr>
            @else
                @for(var check : page.getUrlChecks())
                    <tr>
                        <td>${check.getId()}</td>
                        <td>${check.getStatusCode()}</td>
                        <td>${check.getTitle() != null ? check.getTitle() : ""}</td>
                        <td>${check.getH1() != null ? check.getH1() : ""}</td>
                        <td>${check.getDescription() != null ? check.getDescription() : ""}</td>
                        <td>${check.getFormattedCreatedAt()}</td>
                    </tr>
                @endfor
            @endif
            </tbody>
        </table>

        <a href="${NamedRoutes.urlsPath()}" class="btn btn-secondary mt-3">Назад к списку</a>
    </div>
`
)