using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using Microsoft.AspNetCore.Mvc.Rendering;

namespace lab_3.Pages
{
    public class IndexModel : PageModel
    {
        private readonly ILogger<IndexModel> _logger;
        private readonly DatabaseAPI _databaseAPI;

        public List<TrashContainer> Containers { get; private set; }


        public IndexModel(ILogger<IndexModel> logger)
        {
            _logger = logger;
            _databaseAPI = new DatabaseAPI();
        }

        public void OnGet()
        {
            Containers = _databaseAPI.GetContiners() ?? [];
        }

        public async Task<IActionResult> OnPostDeleteAsync(int id)
        {
            //TODO: Make me asynchronous
            _databaseAPI.RemoveContainer(id);

            return RedirectToPage();
        }


        public async Task<IActionResult> OnPostEditAsync(int id, string city, string street, int house, List<TrashContainer.TrashType> allowedWaste)
        {
            if (!ModelState.IsValid)
            {
                return Page();
            }

            var container = new TrashContainer(id, city, street, house, allowedWaste.ToHashSet());

            var result = await _databaseAPI.UpdateContainerAsync(container);
            if (!result)
            {
                ModelState.AddModelError("", "Error updating container.");
                return Page();
            }

            return RedirectToPage();
        }

        public async Task<IActionResult> OnPostCreateAsync()
        {
            var defaultContainer = new TrashContainer(0, "Default City", "Default Street", 1, []);
            _databaseAPI.AddContainer(defaultContainer);
            return RedirectToPage();
        }
    }
}
