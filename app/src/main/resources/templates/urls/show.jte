@import hexlet.code.dto.UrlPage
@import hexlet.code.util.NamedRoutes
@param UrlPage page

@template.layout.page(
page = page,
content = @`
    <div class="container mt-5">
        <h1>Сайт: ${page.getUrl().getName()}</h1>

        <table class="table table-bordered">
            <tbody>
            <tr>
                <td>ID</td>
                <td>${page.getUrl().getId()}</td>
            </tr>
            <tr>
                <td>Имя</td>
                <td>${page.getUrl().getName()}</td>
            </tr>
            <tr>
                <td>Дата создания</td>
                <td>${page.getUrl().getFormattedCreatedAt()}</td>
            </tr>
            </tbody>
        </table>

        <a href="${NamedRoutes.urlsPath()}" class="btn btn-primary">Назад к списку</a>
    </div>
`
)