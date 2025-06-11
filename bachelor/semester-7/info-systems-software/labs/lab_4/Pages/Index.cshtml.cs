using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using Microsoft.AspNetCore.Mvc.Rendering;
using System.IO;

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

        public async Task<IActionResult> OnPostImportAsync(IFormFile uploadedFile)
        {
            if (uploadedFile == null || uploadedFile.Length == 0)
            {
                ModelState.AddModelError("", "Файл не выбран или пуст.");
                return Page();
            }

            var filePath = Path.Combine(Directory.GetCurrentDirectory(), "uploads", uploadedFile.FileName);

            using (var stream = new FileStream(filePath, FileMode.Create))
            {
                await uploadedFile.CopyToAsync(stream);
            }

            var result = _databaseAPI.ImportTrash(filePath);
            if (!result)
            {
                ModelState.AddModelError("", "Ошибка импорта базы данных.");
                return Page();
            }

            return RedirectToPage();
        }

        public async Task<IActionResult> OnPostExportAsync()
        {
            if (_databaseAPI.ExportTrash())
            {
                return PhysicalFile(Directory.GetCurrentDirectory()+"\\db.xml", "application/octet-stream", "db.xml");
            }
            else
            {
                return NotFound();
            }
        }
    }
}
