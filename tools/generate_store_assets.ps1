Add-Type -AssemblyName System.Drawing

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$out = Join-Path $root "store-assets"
New-Item -ItemType Directory -Force -Path $out | Out-Null

function New-Bitmap($width, $height, [bool]$transparent = $false) {
    $bitmap = New-Object System.Drawing.Bitmap $width, $height, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
    if ($transparent) {
        $graphics.Clear([System.Drawing.Color]::Transparent)
    } else {
        $graphics.Clear([System.Drawing.ColorTranslator]::FromHtml("#F6FAF5"))
    }
    return @{ Bitmap = $bitmap; Graphics = $graphics }
}

function Save-Png($bitmap, $graphics, $path) {
    $graphics.Dispose()
    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bitmap.Dispose()
}

function Brush($hex) {
    return New-Object System.Drawing.SolidBrush ([System.Drawing.ColorTranslator]::FromHtml($hex))
}

function Pen($hex, $width = 1) {
    $pen = New-Object System.Drawing.Pen ([System.Drawing.ColorTranslator]::FromHtml($hex)), $width
    $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $pen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Round
    return $pen
}

function Font($size, $style = [System.Drawing.FontStyle]::Regular) {
    return New-Object System.Drawing.Font "Segoe UI", $size, $style, ([System.Drawing.GraphicsUnit]::Pixel)
}

function RoundedPath($x, $y, $w, $h, $r) {
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    if ($r -le 0) {
        $path.AddRectangle((New-Object System.Drawing.RectangleF ([single]$x, [single]$y, [single]$w, [single]$h)))
        return $path
    }
    $d = $r * 2
    $path.AddArc($x, $y, $d, $d, 180, 90)
    $path.AddArc($x + $w - $d, $y, $d, $d, 270, 90)
    $path.AddArc($x + $w - $d, $y + $h - $d, $d, $d, 0, 90)
    $path.AddArc($x, $y + $h - $d, $d, $d, 90, 90)
    $path.CloseFigure()
    return $path
}

function FillRounded($g, $x, $y, $w, $h, $r, $hex) {
    $path = RoundedPath $x $y $w $h $r
    $brush = Brush $hex
    $g.FillPath($brush, $path)
    $brush.Dispose()
    $path.Dispose()
}

function StrokeRounded($g, $x, $y, $w, $h, $r, $hex, $width = 1) {
    $path = RoundedPath $x $y $w $h $r
    $pen = Pen $hex $width
    $g.DrawPath($pen, $path)
    $pen.Dispose()
    $path.Dispose()
}

function Draw-Text($g, $text, $x, $y, $size, $hex, $style = [System.Drawing.FontStyle]::Regular) {
    $font = Font $size $style
    $brush = Brush $hex
    $g.DrawString($text, $font, $brush, [single]$x, [single]$y)
    $brush.Dispose()
    $font.Dispose()
}

function Draw-CenteredText($g, $text, $rect, $size, $hex, $style = [System.Drawing.FontStyle]::Regular) {
    $font = Font $size $style
    $brush = Brush $hex
    $format = New-Object System.Drawing.StringFormat
    $format.Alignment = [System.Drawing.StringAlignment]::Center
    $format.LineAlignment = [System.Drawing.StringAlignment]::Center
    $g.DrawString($text, $font, $brush, $rect, $format)
    $format.Dispose()
    $brush.Dispose()
    $font.Dispose()
}

function Draw-AppIcon($g, $size) {
    $scale = $size / 512.0
    FillRounded $g (24*$scale) (24*$scale) (464*$scale) (464*$scale) (104*$scale) "#1E6B5C"

    FillRounded $g (156*$scale) (88*$scale) (212*$scale) (304*$scale) (34*$scale) "#1A000000"
    FillRounded $g (144*$scale) (76*$scale) (212*$scale) (304*$scale) (34*$scale) "#FFFFFF"

    $zig = New-Object System.Drawing.Drawing2D.GraphicsPath
    $zig.StartFigure()
    $zig.AddLine((144*$scale), (344*$scale), (164*$scale), (328*$scale))
    $zig.AddLine((164*$scale), (328*$scale), (184*$scale), (344*$scale))
    $zig.AddLine((184*$scale), (344*$scale), (204*$scale), (328*$scale))
    $zig.AddLine((204*$scale), (328*$scale), (224*$scale), (344*$scale))
    $zig.AddLine((224*$scale), (344*$scale), (244*$scale), (328*$scale))
    $zig.AddLine((244*$scale), (328*$scale), (264*$scale), (344*$scale))
    $zig.AddLine((264*$scale), (344*$scale), (284*$scale), (328*$scale))
    $zig.AddLine((284*$scale), (328*$scale), (304*$scale), (344*$scale))
    $zig.AddLine((304*$scale), (344*$scale), (324*$scale), (328*$scale))
    $zig.AddLine((324*$scale), (328*$scale), (356*$scale), (344*$scale))
    $zig.AddLine((356*$scale), (344*$scale), (356*$scale), (380*$scale))
    $zig.AddLine((356*$scale), (380*$scale), (144*$scale), (380*$scale))
    $zig.CloseFigure()
    $white = Brush "#FFFFFF"
    $g.FillPath($white, $zig)
    $white.Dispose()
    $zig.Dispose()

    FillRounded $g (176*$scale) (128*$scale) (116*$scale) (28*$scale) (8*$scale) "#CDEDE5"
    $linePen = Pen "#74AFA2" (12*$scale)
    $g.DrawLine($linePen, (176*$scale), (206*$scale), (292*$scale), (206*$scale))
    $g.DrawLine($linePen, (176*$scale), (250*$scale), (264*$scale), (250*$scale))
    $g.DrawLine($linePen, (176*$scale), (292*$scale), (248*$scale), (292*$scale))
    $linePen.Dispose()

    $tickBrush = Brush "#80D5C2"
    $g.FillEllipse($tickBrush, (270*$scale), (224*$scale), (156*$scale), (156*$scale))
    $tickBrush.Dispose()

    $checkPen = Pen "#FFFFFF" (28*$scale)
    $g.DrawLines($checkPen, @(
        (New-Object System.Drawing.PointF (310*$scale), (304*$scale)),
        (New-Object System.Drawing.PointF (340*$scale), (334*$scale)),
        (New-Object System.Drawing.PointF (392*$scale), (278*$scale))
    ))
    $checkPen.Dispose()
}

function Draw-PhoneFrame($g, $x, $y, $w, $h, $screenTitle, $kind) {
    FillRounded $g $x $y $w $h 38 "#111827"
    FillRounded $g ($x+12) ($y+12) ($w-24) ($h-24) 28 "#F6FAF5"
    FillRounded $g ($x+12) ($y+12) ($w-24) 70 28 "#117A5C"
    Draw-Text $g $screenTitle ($x+34) ($y+34) 24 "#FFFFFF" ([System.Drawing.FontStyle]::Bold)

    if ($kind -eq "dashboard") {
        Draw-Text $g "Maio de 2026" ($x+190) ($y+96) 24 "#1B1F1D" ([System.Drawing.FontStyle]::Bold)
        Draw-SummaryCards $g ($x+34) ($y+142) 2
        Draw-AdSlot $g ($x+34) ($y+310) ($w-68) 88
        Draw-Chips $g ($x+34) ($y+426)
        Draw-Text $g "Vence primeiro" ($x+34) ($y+548) 22 "#1B1F1D" ([System.Drawing.FontStyle]::Bold)
        Draw-BillCard $g ($x+34) ($y+590) ($w-68) "Internet" "R$ 119,90" "Vence hoje" "#D97706"
        Draw-BillCard $g ($x+34) ($y+688) ($w-68) "Aluguel" "R$ 1.450,00" "Em aberto" "#117A5C"
    } elseif ($kind -eq "summary") {
        Draw-Text $g "Resumo mensal" ($x+34) ($y+100) 28 "#1B1F1D" ([System.Drawing.FontStyle]::Bold)
        Draw-SummaryCards $g ($x+34) ($y+158) 2
        Draw-AdSlot $g ($x+34) ($y+326) ($w-68) 72
        Draw-Text $g "Por categoria" ($x+34) ($y+430) 23 "#1B1F1D" ([System.Drawing.FontStyle]::Bold)
        Draw-SummaryLine $g ($x+34) ($y+478) ($w-68) "Casa" "3 contas" "R$ 1.820,00"
        Draw-SummaryLine $g ($x+34) ($y+538) ($w-68) "Internet" "1 conta" "R$ 119,90"
        Draw-SummaryLine $g ($x+34) ($y+598) ($w-68) "Cartao" "2 contas" "R$ 840,50"
    } else {
        Draw-Text $g "Nova conta" ($x+34) ($y+100) 28 "#1B1F1D" ([System.Drawing.FontStyle]::Bold)
        Draw-Input $g ($x+34) ($y+160) ($w-68) "Nome da conta" "Internet"
        Draw-Input $g ($x+34) ($y+244) ($w-68) "Valor" "R$ 119,90"
        Draw-Input $g ($x+34) ($y+328) ($w-68) "Vencimento" "25/05/2026"
        Draw-Input $g ($x+34) ($y+412) ($w-68) "Categoria" "Internet"
        FillRounded $g ($x+34) ($y+530) ($w-68) 58 16 "#117A5C"
        Draw-CenteredText $g "Salvar conta" (New-Object System.Drawing.RectangleF (($x+34), ($y+530), ($w-68), 58)) 22 "#FFFFFF" ([System.Drawing.FontStyle]::Bold)
    }
}

function Draw-SummaryCards($g, $x, $y, $cols) {
    $cards = @(
        @("Total", "R$ 2.409,40", "#117A5C"),
        @("Pago", "R$ 839,50", "#2E7D32"),
        @("Aberto", "R$ 1.569,90", "#B7791F"),
        @("Atrasado", "R$ 0,00", "#C62828")
    )
    for ($i = 0; $i -lt $cards.Count; $i++) {
        $cx = $x + (($i % $cols) * 190)
        $cy = $y + ([Math]::Floor($i / $cols) * 94)
        FillRounded $g $cx $cy 176 76 14 "#FFFFFF"
        StrokeRounded $g $cx $cy 176 76 14 "#DDE8E2" 1
        FillRounded $g $cx $cy 6 76 3 $cards[$i][2]
        Draw-Text $g $cards[$i][0] ($cx+18) ($cy+14) 16 $cards[$i][2] ([System.Drawing.FontStyle]::Bold)
        Draw-Text $g $cards[$i][1] ($cx+18) ($cy+40) 19 "#202521" ([System.Drawing.FontStyle]::Bold)
    }
}

function Draw-AdSlot($g, $x, $y, $w, $h) {
    FillRounded $g $x $y $w $h 16 "#EFF7F4"
    Draw-CenteredText $g "Anuncio" (New-Object System.Drawing.RectangleF ($x, $y, $w, $h)) 16 "#7B918A"
}

function Draw-Chips($g, $x, $y) {
    $chips = @("Todos", "Em aberto", "Pagos", "Atrasados")
    $cx = $x
    foreach ($chip in $chips) {
        $w = 84 + ($chip.Length * 4)
        FillRounded $g $cx $y $w 36 12 $(if ($chip -eq "Todos") { "#FFE19C" } else { "#FFFFFF" })
        StrokeRounded $g $cx $y $w 36 12 "#C9D5CF" 1
        Draw-CenteredText $g $chip (New-Object System.Drawing.RectangleF ($cx, $y, $w, 36)) 15 "#1F2A25"
        $cx += $w + 12
    }
}

function Draw-BillCard($g, $x, $y, $w, $title, $amount, $status, $color) {
    FillRounded $g $x $y $w 78 14 "#FFFFFF"
    StrokeRounded $g $x $y $w 78 14 "#DDE8E2" 1
    FillRounded $g $x $y 6 78 3 $color
    Draw-Text $g $title ($x+20) ($y+12) 20 "#1B1F1D" ([System.Drawing.FontStyle]::Bold)
    Draw-Text $g $status ($x+20) ($y+42) 15 $color ([System.Drawing.FontStyle]::Bold)
    Draw-Text $g $amount ($x+$w-150) ($y+24) 19 "#1B1F1D" ([System.Drawing.FontStyle]::Bold)
}

function Draw-SummaryLine($g, $x, $y, $w, $title, $subtitle, $value) {
    FillRounded $g $x $y $w 50 12 "#FFFFFF"
    Draw-Text $g $title ($x+16) ($y+8) 17 "#1B1F1D" ([System.Drawing.FontStyle]::Bold)
    Draw-Text $g $subtitle ($x+16) ($y+29) 13 "#65756E"
    Draw-Text $g $value ($x+$w-150) ($y+15) 17 "#117A5C" ([System.Drawing.FontStyle]::Bold)
}

function Draw-Input($g, $x, $y, $w, $label, $value) {
    Draw-Text $g $label $x ($y-24) 15 "#5B6B64"
    FillRounded $g $x $y $w 58 12 "#FFFFFF"
    StrokeRounded $g $x $y $w 58 12 "#CAD8D1" 1
    Draw-Text $g $value ($x+18) ($y+16) 19 "#202521"
}

function Draw-StoreIcon($size, $path) {
    $img = New-Bitmap $size $size $true
    Draw-AppIcon $img.Graphics $size
    Save-Png $img.Bitmap $img.Graphics $path
}

Draw-StoreIcon 512 (Join-Path $out "icon-512.png")
Draw-StoreIcon 114 (Join-Path $out "icon-114.png")

$promo = New-Bitmap 1024 500 $false
$g = $promo.Graphics
FillRounded $g 0 0 1024 500 0 "#F6FAF5"
FillRounded $g 0 0 1024 500 0 "#EAF5EF"
FillRounded $g 52 52 210 210 46 "#1E6B5C"
$iconClip = New-Bitmap 210 210 $true
Draw-AppIcon $iconClip.Graphics 210
$g.DrawImage($iconClip.Bitmap, 52, 52, 210, 210)
$iconClip.Graphics.Dispose()
$iconClip.Bitmap.Dispose()
Draw-Text $g "Conta em Dia" 308 82 50 "#123D33" ([System.Drawing.FontStyle]::Bold)
Draw-Text $g "Organize boletos e vencimentos" 310 154 27 "#24584C" ([System.Drawing.FontStyle]::Regular)
Draw-Text $g "Controle contas abertas, atrasadas e pagas" 312 206 22 "#5A6F66"
Draw-Text $g "com um resumo mensal simples." 312 238 22 "#5A6F66"
FillRounded $g 312 318 158 52 18 "#117A5C"
Draw-CenteredText $g "Boletos" (New-Object System.Drawing.RectangleF (312,318,158,52)) 20 "#FFFFFF" ([System.Drawing.FontStyle]::Bold)
FillRounded $g 492 318 206 52 18 "#FFE19C"
Draw-CenteredText $g "Vencimentos" (New-Object System.Drawing.RectangleF (492,318,206,52)) 20 "#3F3217" ([System.Drawing.FontStyle]::Bold)
FillRounded $g 720 318 154 52 18 "#80D5C2"
Draw-CenteredText $g "Resumo" (New-Object System.Drawing.RectangleF (720,318,154,52)) 20 "#123D33" ([System.Drawing.FontStyle]::Bold)
Save-Png $promo.Bitmap $promo.Graphics (Join-Path $out "promotional-1024x500.png")

$screens = @(
    @("tablet-dashboard-1280x800.png", "Controle suas contas", "dashboard"),
    @("tablet-summary-1280x800.png", "Resumo financeiro", "summary"),
    @("tablet-form-1280x800.png", "Cadastro rapido", "form")
)

foreach ($screen in $screens) {
    $img = New-Bitmap 1280 800 $false
    $g = $img.Graphics
    FillRounded $g 0 0 1280 800 0 "#F6FAF5"
    FillRounded $g 0 0 1280 120 0 "#117A5C"
    Draw-Text $g "Conta em Dia" 72 42 36 "#FFFFFF" ([System.Drawing.FontStyle]::Bold)
    Draw-Text $g $screen[1] 72 150 42 "#123D33" ([System.Drawing.FontStyle]::Bold)
    Draw-Text $g "Uma forma simples de acompanhar boletos, vencimentos e pagamentos mensais." 74 210 24 "#5A6F66"
    Draw-PhoneFrame $g 720 70 430 690 "Conta em Dia" $screen[2]

    if ($screen[2] -eq "dashboard") {
        Draw-SummaryCards $g 74 300 2
        Draw-BillCard $g 74 520 520 "Internet" "R$ 119,90" "Vence hoje" "#D97706"
        Draw-BillCard $g 74 620 520 "Aluguel" "R$ 1.450,00" "Em aberto" "#117A5C"
    } elseif ($screen[2] -eq "summary") {
        Draw-SummaryCards $g 74 300 2
        Draw-SummaryLine $g 74 530 520 "Casa" "3 contas no mes" "R$ 1.820,00"
        Draw-SummaryLine $g 74 600 520 "Cartao" "2 contas no mes" "R$ 840,50"
    } else {
        Draw-Input $g 74 330 520 "Nome da conta" "Internet"
        Draw-Input $g 74 430 520 "Valor" "R$ 119,90"
        Draw-Input $g 74 530 520 "Vencimento" "25/05/2026"
        FillRounded $g 74 650 240 58 18 "#117A5C"
        Draw-CenteredText $g "Salvar" (New-Object System.Drawing.RectangleF (74,650,240,58)) 22 "#FFFFFF" ([System.Drawing.FontStyle]::Bold)
    }
    Save-Png $img.Bitmap $img.Graphics (Join-Path $out $screen[0])
}

Write-Host "Assets generated at $out"
