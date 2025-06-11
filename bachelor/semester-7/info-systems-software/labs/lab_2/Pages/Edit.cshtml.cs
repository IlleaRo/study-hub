using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace lab_2.Pages
{
    public class EditModel : PageModel
    {
        private readonly ILogger<EditModel> _logger;

        public EditModel(ILogger<EditModel> logger)
        {
            _logger = logger;
        }

        public void OnGet()
        {
        }
    }

}
