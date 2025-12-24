# leo-legacy.be Static Site

This directory contains a static copy of the leo-legacy.be website.

## Contents

- **leo-legacy.be/** - All website files including HTML, CSS, and images
  - 106 recipes across 15 categories
  - All recipe pages, navigation, and resources

## How to Serve Locally

### Option 1: Python HTTP Server (Recommended)
```bash
cd leo-legacy.be
python3 -m http.server 8000
```
Then visit: http://localhost:8000

### Option 2: Node.js HTTP Server
```bash
cd leo-legacy.be
npx http-server -p 8000
```
Then visit: http://localhost:8000

### Option 3: PHP Built-in Server
```bash
cd leo-legacy.be
php -S localhost:8000
```
Then visit: http://localhost:8000

## Structure

- `index.html` - Homepage
- `rcpt_menu.php?*.html` - Recipe category pages
- `menu_detail.php?id=*.html` - Individual recipe pages
- `bmi.php.html` - BMI calculator
- `links.php.html` - Cooking links
- `gastb_lezen.php.html` - Guest book
- `images/` - All website images
- `leo-legacy.css` - Stylesheet

## Notes

- All PHP pages have been saved as .html files with converted links
- All resources (CSS, images) are included and links have been updated
- The site is fully functional as a static website
